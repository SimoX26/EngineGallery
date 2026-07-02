# Relazione di Sicurezza del Sistema Software EngineGallery

Data: 4 aprile 2026  
Versione analizzata: stato corrente del repository

## 1. Scopo e perimetro
Questa relazione descrive **come funziona la sicurezza** del sistema EngineGallery, composto da:
- backend web Java Servlet/JSP (autenticazione, sessioni, upload, accesso file)
- web client browser
- wrapper Android Kotlin (WebView)

La relazione è basata su analisi del codice sorgente, non su penetration test black-box.

## 2. Architettura di sicurezza (vista sintetica)
Il modello di sicurezza è centrato su:
- autenticazione utente con credenziali (`/auth`)
- sessione server-side (`HttpSession`, attributo `loggedUser`)
- persistenza login con cookie `remember_user_id` firmato
- filtro globale `AuthenticationFilter` su tutte le route (`/*`)
- protezione CSRF su tutte le richieste `POST`
- validazione input per route sensibili (parametri, path, filename, id)
- controlli anti path traversal per lettura file da disco

## 3. Autenticazione e gestione credenziali

### 3.1 Flusso login web
File principali:
- `src/main/java/it/SimoSW/controller/gui/AuthenticationServlet.java`
- `src/main/java/it/SimoSW/controller/app/AuthenticationController.java`

Flusso:
1. `POST /auth` riceve `username/password`.
2. `AuthenticationController.login(...)` carica l'utente dal DAO.
3. Verifica password via `PasswordHashUtil.verify(...)`.
4. Se credenziali valide:
   - invalida eventuale sessione precedente (mitigazione session fixation)
   - crea nuova sessione
   - setta `loggedUser` in sessione
   - emette cookie remember-me firmato
5. Se invalide: ritorna pagina login con errore.

### 3.2 Hash password
File principale:
- `src/main/java/it/SimoSW/util/security/PasswordHashUtil.java`

Implementazione:
- algoritmo principale: `PBKDF2WithHmacSHA256`
- iterazioni: `120_000`
- salt random per utente (16 byte)
- formato hash persistito: `pbkdf2$iterations$salt$derivedKey`

Compatibilità legacy:
- sono ancora accettati hash legacy SHA-256 (senza prefisso `pbkdf2$`)
- al primo login riuscito con hash legacy avviene **migrazione trasparente** a PBKDF2 (`updatePasswordHash`)

Valutazione:
- buona compatibilità operativa in transizione
- miglioramento netto rispetto a SHA-256 semplice

## 4. Sessione applicativa e remember-me

### 4.1 Sessione server-side
File:
- `AuthenticationServlet.java`
- `AuthenticationFilter.java`

Punti chiave:
- la sessione autenticata è server-side (`HttpSession`)
- utente loggato identificato da `session.getAttribute("loggedUser")`
- session fixation mitigata invalidando la sessione pre-login

### 4.2 Remember-me token
File:
- `src/main/java/it/SimoSW/util/security/RememberMeTokenUtil.java`
- `src/main/java/it/SimoSW/util/security/CookieSecurityUtil.java`

Meccanismo:
- cookie: `remember_user_id`
- valore cookie non è un ID nudo, ma token firmato HMAC-SHA256:
  - payload: `userId:expiresAt:nonce`
  - signature: HMAC(payload, secret)
- validazione in `AuthenticationFilter.ensureAutoLogin(...)`
- token ruotato dopo auto-login riuscito

Attributi cookie:
- `HttpOnly`
- `SameSite=Lax`
- `Secure` abilitato solo se richiesta HTTPS (`request.isSecure()`)
- `Path` limitato al context path applicativo

Segreto di firma:
- configurabile via:
  - system property `enginegallery.remember.secret`
  - env `ENGINE_GALLERY_REMEMBER_SECRET`
- fallback attuale: secret effimero random a runtime se non configurato

Effetto del fallback effimero:
- i token remember-me possono invalidarsi dopo riavvio backend
- non è una falla di confidenzialità, ma un limite di continuità login

## 5. Autorizzazione e controllo accessi

File:
- `src/main/java/it/SimoSW/controller/gui/AuthenticationFilter.java`

Logica:
- filtro globale su `/*`
- path pubblici espliciti:
  - `/`, `/home`, `/auth`, `/index.jsp`, `/logout`, `/assets/*`, `/image/share`
- tutto il resto richiede sessione autenticata
- se non autenticato: redirect a `/auth`

Osservazione importante:
- non emerge un controllo autorizzativo per ruoli (RBAC) nel filtro
- sembra quindi un modello "authenticated user can access app" senza separazioni privilegio forti

## 6. Protezione CSRF

File:
- `src/main/java/it/SimoSW/util/security/CsrfTokenUtil.java`
- `src/main/java/it/SimoSW/controller/gui/AuthenticationFilter.java`
- JSP form con campo hidden `csrfToken`

Meccanismo:
1. su richieste `GET` applicative viene garantito token in sessione (`csrf_token`)
2. su tutte le `POST` (escluse route statiche/assets/uploads) viene validato `csrfToken`
3. token confrontato in constant-time con valore in sessione
4. invalid token => `403 Forbidden`

Copertura pratica:
- login/logout
- edit/delete customer/engine/warehouse
- upload motore
- creazione prova idraulica
- quick status update

Valutazione:
- protezione CSRF ben impostata lato server
- dipende dalla presenza del campo hidden nelle view POST (attualmente presente nei form principali)

## 7. Sicurezza upload e accesso file

### 7.1 Upload immagini motore
File:
- `src/main/java/it/SimoSW/controller/gui/UploadServlet.java`
- `src/main/java/it/SimoSW/util/ImageOptimizationUtil.java`

Controlli presenti:
- `@MultipartConfig` con limiti dimensione richiesta/file
- accettazione solo parti `images` con `contentType` che inizia con `image/`
- conversione/ottimizzazione immagini a JPEG con nome random UUID
- sanitizzazione filename fallback
- directory di destinazione risolta e normalizzata

Nota tecnica:
- se `ImageIO.read(...)` fallisce, viene usato fallback `part.write(...)` (file raw)
- in quel ramo il controllo forte di "è davvero un'immagine" è più debole e si basa sul content type dichiarato

### 7.2 Download file da storage
File:
- `UploadImageServlet.java`
- `UploadHydraulicVideoServlet.java`
- `UploadWarehouseImageServlet.java`
- `ImageShareServlet.java`

Controlli presenti:
- validazione formato parametri (`engineRef`, `itemId`, index)
- blocco pattern path traversal (`..`, `/`, `\\`)
- costruzione path con `Path.resolve(...).normalize()`
- check `startsWith(uploadBase)` per confinamento nella root autorizzata
- check esistenza e tipo file

Valutazione:
- difese contro traversal solide
- endpoint `/image/share` è volutamente pubblico (condivisione), da considerare scelta di business

## 8. Configurazione DB e segreti

File:
- `src/main/java/it/SimoSW/model/dao/database/ConnectionFactory.java`

Supporto configurazione:
- property/env per URL, user, password DB

Stato attuale:
- la configurazione DB deve essere fornita esternamente tramite property/env già previste dal progetto

Impatto sicurezza:
- l'applicazione non deve usare fallback hardcoded per URL, utente o password
- l'assenza della configurazione obbligatoria deve bloccare l'inizializzazione

## 9. Difese XSS lato view

Evidenza:
- login error in view usa `c:out` (output escaped)

Valutazione generale:
- molte view usano EL/JSTL, che riduce rischio se non si concatena HTML/JS non escaped
- resta fondamentale mantenere escape rigoroso in attributi HTML e script inline

## 10. Sicurezza Android wrapper (Kotlin)

File:
- `android-app/app/src/main/java/it/simosw/enginegallery/MainActivity.kt`
- `android-app/app/src/main/AndroidManifest.xml`
- `android-app/app/src/main/res/xml/network_security_config.xml`
- `android-app/app/src/main/res/xml/file_paths.xml`

### 10.1 Sessione/cookie WebView
- cookie first-party abilitati (`setAcceptCookie(true)`)
- third-party cookie disabilitati (hardening privacy/superficie)
- persistenza cookie su lifecycle (`CookieManager.flush()` in `onPause`)

### 10.2 WebView sicurezza operativa
- JavaScript abilitato (necessario alla webapp)
- override URL: `http/https` in WebView, `tel/mailto` via intent esterno
- upload camera via `FileProvider` con path limitato a cache app (`cache-path camera/`)

### 10.3 Trasporto rete Android
- `usesCleartextTraffic="true"`
- `network_security_config` permette cleartext a livello base

Impatto:
- utile per LAN/dev, ma riduce sicurezza in reti non fidate
- in produzione è consigliato solo HTTPS

## 11. Logging ed error handling

Miglioramenti presenti:
- sostituiti vari `System.out/printStackTrace` con `java.util.logging` in punti sensibili

Rischio residuo:
- verificare che i log in produzione non espongano dati sensibili (token, parametri critici)

## 12. Minacce coperte e non coperte

### 12.1 Minacce ben coperte
- session fixation (mitigata)
- cookie tampering remember-me (firma HMAC)
- CSRF su POST (mitigata)
- path traversal su file serving (mitigata)

### 12.2 Minacce parzialmente/non coperte
- brute-force login (mancano rate limiting/lockout/captcha)
- hardening TLS end-to-end (Android cleartext consentito)
- autorizzazione fine per ruolo/permesso (RBAC non evidente nel filtro)
- upload spoofing avanzato nel ramo fallback non-ImageIO
- secret management rigoroso in produzione (fallback DB/remember-secret)

## 13. Priorità di miglioramento consigliata

### P0 (alta priorità)
- imporre HTTPS in produzione (backend + Android release)
- configurare sempre `ENGINE_GALLERY_REMEMBER_SECRET` stabile
- eliminare fallback credenziali DB in produzione

### P1 (media-alta)
- rate limiting su endpoint login
- introdurre lockout progressivo e audit tentativi login
- validazione contenuto file più forte (magic number/MIME sniffing server-side)

### P2 (media)
- modello autorizzativo RBAC esplicito su servlet sensibili
- header di sicurezza HTTP centralizzati (CSP, X-Frame-Options/frame-ancestors, Referrer-Policy, HSTS in HTTPS)
- test automatici sicurezza (unit + integration) su auth/csrf/file traversal

## 14. Conclusione
Il sistema ha una base di sicurezza migliorata e coerente per autenticazione/sessione/csrf e difesa file traversal.  
I principali gap residui sono di tipo "hardening produzione" (TLS obbligatorio, gestione segreti, anti brute-force, autorizzazione per ruolo).

In sintesi: **buon livello per ambiente controllato**, da completare con hardening operativo per scenario Internet/produzione.

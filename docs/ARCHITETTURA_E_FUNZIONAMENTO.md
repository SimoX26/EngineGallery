# Architettura e funzionamento di Engine Gallery

Data di revisione: 6 settembre 2026
Perimetro: web application Java e modulo `android-app/` presenti nel repository.

## 1. Vista d'insieme

Engine Gallery e una web application MVC server-side per gestire clienti, motori, immagini, prove idrauliche, magazzino, catalogo e attivita degli utenti. Il progetto Maven produce un file WAR da eseguire in un container compatibile con Servlet 4.0 e JSP 2.3. Il modulo Android non contiene un backend distinto: visualizza la web application in una WebView e aggiunge acquisizione file, fotocamera e condivisione nativa.

```text
Browser / WebView Android
          |
          v
Servlet e AuthenticationFilter
          |
          v
Controller applicativi
          |
          v
Interfacce DAO -> DAO JDBC -> MySQL
          |
          +----> filesystem degli upload
```

## 2. Strati della web application

### Presentazione e routing

Le classi in `src/main/java/it/SimoSW/controller/gui` espongono le route tramite `@WebServlet`. Le servlet leggono e validano i parametri HTTP, invocano il livello applicativo e inoltrano a JSP sotto `WEB-INF/views`, non accessibili direttamente dal client. `AuthenticationFilter`, registrato su `/*`, applica autenticazione e controllo CSRF. `web.xml` dichiara soltanto l'applicazione; routing, filtro e listener usano annotazioni.

Le risorse statiche sono in `src/main/webapp/assets`. L'interfaccia e renderizzata sul server con JSP/JSTL e usa JavaScript per ricerca, visualizzazione e condivisione media.

### Logica applicativa

Le classi in `src/main/java/it/SimoSW/controller/app` coordinano casi d'uso e trasformazioni tra entita, bean e DAO. I controller non gestiscono direttamente richieste o risposte HTTP.

### Dominio e persistenza

`src/main/java/it/SimoSW/model` contiene entita, enum e contratti DAO. Le implementazioni JDBC in `model/dao/database` usano `PreparedStatement` e connessioni create da `ConnectionFactory`. Non e presente un connection pool nel codice applicativo: ogni operazione DAO apre una connessione tramite `DriverManager` e la chiude con try-with-resources.

Lo schema completo e in `src/main/resources/db.sql`; le modifiche incrementali sono in `src/main/resources/migrations`. Le entita persistite principali sono:

- `users`, con ruolo `ADMIN` o `OPERATOR` e hash password;
- `customers`;
- `engines`, collegati ai clienti;
- `images`, collegate ai motori;
- `hydraulic_tests`;
- `warehouse_items` e `warehouse_images`;
- `catalog_items`;
- `user_activity_log`.

Le migration sono script SQL manuali: il codice non include un migration runner automatico.

### Bootstrap

`ApplicationContextListener` crea `ApplicationInitializer` all'avvio del contesto servlet. L'initializer costruisce una singola `ConnectionFactory`, i DAO e i controller, quindi li rende disponibili alle servlet tramite il `ServletContext`.

## 3. Flussi applicativi

### Accesso e sessione

1. `POST /auth` cerca l'utente tramite `AuthenticationController`.
2. `PasswordHashUtil` verifica PBKDF2-HMAC-SHA256; gli hash SHA-256 legacy validi vengono aggiornati a PBKDF2.
3. La servlet invalida la sessione precedente, crea una nuova sessione e salva `loggedUser`.
4. Viene emesso un token remember-me firmato HMAC nel cookie `remember_user_id`.
5. Alle richieste successive `AuthenticationFilter` accetta la sessione oppure verifica il cookie e ricrea la sessione.

Il filtro lascia pubblici `/`, `/home`, `/auth`, `/index.jsp`, `/logout`, `/assets/*` e `/image/share`. Tutte le altre route richiedono un utente autenticato. I controlli di ruolo sono locali alle funzionalita amministrative: statistiche, manutenzione e archivio motori verificano `ADMIN`; le altre operazioni autenticate sono disponibili anche a `OPERATOR`.

### Protezione delle modifiche

`CsrfTokenUtil` conserva un token casuale in sessione. Il filtro lo verifica su ogni `POST`, eccetto URL sotto `/assets/` e `/uploads/`. I form JSP devono includere il parametro `csrfToken`. I flussi di scrittura applicano in genere il pattern POST/Redirect/GET e registrano le operazioni principali tramite `UserActivityAuditLogger`.

### Clienti e motori

Le aree clienti e motori espongono lista, dettaglio, inserimento/modifica ed eliminazione. Un motore appartiene a un cliente, possiede un riferimento nel formato `RML-AAAA-NNNNN`, uno stato (`WAITING`, `WORK_IN_PROGRESS`, `READY`, `DELIVERED`) e puo avere immagini. L'archivio corrisponde ai motori consegnati ed e riservato agli amministratori.

### Prove idrauliche

Le prove idrauliche gestiscono dati di test e un video. In creazione il video e salvato temporaneamente e, oltre una soglia, il server tenta la compressione invocando `ffmpeg`; in caso di fallimento conserva il file originale. La dimensione massima dichiarata dalla servlet e 500 MB per file e 700 MB per richiesta.

### Magazzino e catalogo

Il magazzino consente CRUD di articoli e immagini associate. Il catalogo e al momento consultivo e legge le schede tecniche da `catalog_items`. La pagina pronta consegna e un placeholder, distinto dalla vista dei motori pronti.

### Upload e filesystem

`UploadPathResolver` sceglie una root scrivibile, privilegiando la configurazione esplicita e usando fallback locali se necessario. Sotto la root crea directory separate per motori, prove idrauliche e magazzino. Le servlet di download normalizzano i path e verificano che rimangano nella directory prevista.

Le immagini decodificabili vengono orientate, ridimensionate e convertite in JPEG da `ImageOptimizationUtil`. Se `ImageIO` non decodifica il contenuto, il file viene conservato con nome normalizzato: questo comportamento e rilevante per la sicurezza.

`GET /image/share` rende pubblica un'immagine individuata da riferimento motore e indice; le altre route `/uploads/...` richiedono autenticazione.

## 4. Modulo Android

`android-app/` e un progetto Gradle/Kotlin separato, con Java/Kotlin 17, `minSdk 24` e `targetSdk 35`. `MainActivity` carica `BuildConfig.ENGINE_GALLERY_BASE_URL`, di default `https://rettificamotorilacroce.it`, in una WebView con JavaScript, cookie first-party e storage DOM abilitati.

Il wrapper annulla gli errori TLS, aggiorna a HTTPS i link HTTP diretti agli host Engine Gallery conosciuti, gestisce `tel:` e `mailto:` con applicazioni esterne, permette scelta multipla e fotocamera ed espone un bridge JavaScript per la condivisione Android. I link HTTP/HTTPS verso host diversi restano caricabili nella stessa WebView; le implicazioni sono riportate nel documento sulle vulnerabilita.

## 5. Configurazione operativa

| Funzione | Property JVM | Variabile ambiente |
|---|---|---|
| URL database | `enginegallery.db.url` | `ENGINE_GALLERY_DB_URL` |
| Utente database | `enginegallery.db.user` | `ENGINE_GALLERY_DB_USER` |
| Password database | `enginegallery.db.password` | `ENGINE_GALLERY_DB_PASSWORD` |
| Root upload | `enginegallery.upload.dir` | `ENGINE_GALLERY_UPLOAD_DIR` |
| Secret remember-me | `enginegallery.remember.secret` | `ENGINE_GALLERY_REMEMBER_SECRET` |
| File secret remember-me | `enginegallery.remember.secret.file` | `ENGINE_GALLERY_REMEMBER_SECRET_FILE` |

URL, utente e password DB sono obbligatori. Il secret remember-me puo essere letto da un file configurato; senza configurazione esplicita viene letto o creato in `${user.home}/.enginegallery/remember-secret.key`, con fallback effimero solo se il file non e utilizzabile.

## 6. Build e verifica

Dalla root si usano `mvn test` e `mvn clean package`; il WAR risultante e in `target/`. Per Android si usa `./build-apk.sh`, oppure `./gradlew assembleDebug` e `./gradlew assembleRelease` da `android-app/`.

Gli script `deploy-remoto.sh`, `backup-remoto.sh` e `provision-db-user.sh` sono operazioni amministrative manuali e non fanno parte dell'avvio automatico.

## 7. Mappa del repository

| Percorso | Responsabilita |
|---|---|
| `src/main/java/.../controller/gui` | HTTP, routing, sessione e view model |
| `src/main/java/.../controller/app` | casi d'uso applicativi |
| `src/main/java/.../model` | dominio e contratti DAO |
| `src/main/java/.../model/dao/database` | persistenza JDBC/MySQL |
| `src/main/java/.../util` | bootstrap, sicurezza, upload, audit e navigazione |
| `src/main/webapp/WEB-INF/views` | JSP server-side |
| `src/main/webapp/assets` | CSS, JavaScript e immagini statiche |
| `src/main/resources` | schema e migration SQL |
| `src/test` | test JUnit disponibili |
| `android-app` | wrapper Android WebView |
| `docs` | documentazione tecnica e di sicurezza |

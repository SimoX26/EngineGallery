## 🏎️ Engine Gallery

**Engine Gallery** è una web application Java sviluppata con **architettura MVC** che permette di gestire e consultare una galleria strutturata di **motori e relative immagini**, organizzate in cartelle e sottocartelle, tendendo traccia degli stati di lavorazione.

Il progetto nasce con l’obiettivo di realizzare una **galleria tecnica accessibile via web**, pensata per officine o archivi industriali, mantenendo una forte separazione tra **logica applicativa, persistenza e presentazione**.


#### ✨ Funzionalità principali

* Visualizzazione di una **galleria di motori**
* Organizzazione delle immagini in **cartelle e sottocartelle**
* Associazione di **metadati** (cliente, codice motore)
* Gestione dello **stato di lavorazione del motore**
* Ricerca e filtraggio dei contenuti
* Interfaccia responsive accessibile da desktop e mobile


#### 🧱 Architettura e tecnologie

* **Java + Maven**
* **Servlet & JSP**
* **Pattern MVC**
* **DAO pattern** per l’accesso ai dati
* Persistenza **database** (con astrazione del layer)
* **Tomcat**
* Frontend con **HTML, CSS**

L’applicazione è progettata per essere **scalabile, estendibile e manutenibile**, con particolare attenzione alle buone pratiche di ingegneria del software.

#### 📱 Versione Android installabile

Nel repository è presente il progetto Android in:

`android-app/`

Guida build/install APK:

`android-app/README.md`

#### 📁 Percorso di salvataggio immagini

Per default le immagini vengono salvate nella cartella:

```
~/EngineGallery/uploads/engines
```

Puoi personalizzare il percorso con:

- variabile ambiente `ENGINE_GALLERY_UPLOAD_DIR`
- system property `enginegallery.upload.dir`

#### 🔐 Configurazione sicurezza/DB

Per avviare l'applicazione ora sono richieste le configurazioni DB via variabili esterne (niente credenziali hardcoded):

- `ENGINE_GALLERY_DB_URL` (oppure `-Denginegallery.db.url=...`)
- `ENGINE_GALLERY_DB_USER` (oppure `-Denginegallery.db.user=...`)
- `ENGINE_GALLERY_DB_PASSWORD` (oppure `-Denginegallery.db.password=...`)

Per firmare in modo sicuro il cookie "remember me" imposta:

- `ENGINE_GALLERY_REMEMBER_SECRET` (oppure `-Denginegallery.remember.secret=...`)

#### 🛠️ Provisioning database

Per la rimozione delle credenziali DB hardcoded non serve una migrazione dello schema: schema e dati applicativi non cambiano. Serve invece provisioning sicuro dell'utente MySQL applicativo e configurazione esterna obbligatoria.

Nuova installazione:

1. Imposta le configurazioni applicative richieste:
   - `ENGINE_GALLERY_DB_URL`
   - `ENGINE_GALLERY_DB_USER`
   - `ENGINE_GALLERY_DB_PASSWORD`
2. Crea lo schema base con un account MySQL amministrativo o già autorizzato:

```bash
mysql --login-path admin-local < src/main/resources/db.sql
```

3. Applica tutte le migrazioni SQL versionate in ordine lessicografico:

```bash
for f in src/main/resources/migrations/*.sql; do
  mysql --login-path admin-local < "$f"
done
```

4. Crea o aggiorna l'utente MySQL dedicato all'applicazione con privilegi minimi:

```bash
ENGINE_GALLERY_DB_URL='jdbc:mysql://db-host:3306/engine_gallery?serverTimezone=UTC' \
ENGINE_GALLERY_DB_USER='app_user' \
ENGINE_GALLERY_DB_PASSWORD='APP_PASSWORD_NON_VERSIONATA' \
./provision-db-user.sh --mysql-user-host app-host --login-path admin-local
```

5. Avvia l'applicazione con variabili d'ambiente oppure con property JVM:

```bash
export ENGINE_GALLERY_DB_URL='jdbc:mysql://db-host:3306/engine_gallery?serverTimezone=UTC'
export ENGINE_GALLERY_DB_USER='app_user'
export ENGINE_GALLERY_DB_PASSWORD='APP_PASSWORD_NON_VERSIONATA'
```

```bash
-Denginegallery.db.url='jdbc:mysql://db-host:3306/engine_gallery?serverTimezone=UTC' \
-Denginegallery.db.user='app_user' \
-Denginegallery.db.password='APP_PASSWORD_NON_VERSIONATA'
```

Installazione esistente:

1. Non rieseguire `src/main/resources/db.sql` su un database già popolato.
2. Mantieni schema e dati esistenti; applica solo eventuali migrazioni SQL mancanti.
3. Crea oppure ruota in modo sicuro l'account MySQL applicativo con `./provision-db-user.sh`.
4. Aggiorna l'ambiente runtime dell'applicazione con una delle coppie supportate:
   - `ENGINE_GALLERY_DB_URL` / `-Denginegallery.db.url`
   - `ENGINE_GALLERY_DB_USER` / `-Denginegallery.db.user`
   - `ENGINE_GALLERY_DB_PASSWORD` / `-Denginegallery.db.password`
5. Riavvia l'applicazione e verifica la connessione.

Permessi applicativi assegnati:

- `SELECT`
- `INSERT`
- `UPDATE`
- `DELETE`

Questi permessi vengono concessi solo sul database applicativo estratto da `ENGINE_GALLERY_DB_URL`. Lo script non assegna privilegi globali o amministrativi.

Verifica della connessione:

- senza configurazione obbligatoria l'avvio fallisce con un errore esplicito che indica la property/env mancante;
- con credenziali errate la connessione JDBC fallisce senza fallback;
- con credenziali corrette l'applicazione completa l'inizializzazione e risponde normalmente.

Risoluzione problemi operativi:

- errore `Missing required database ... configuration`: valorizza `ENGINE_GALLERY_DB_URL`, `ENGINE_GALLERY_DB_USER`, `ENGINE_GALLERY_DB_PASSWORD` oppure le corrispondenti property JVM;
- errore di autenticazione MySQL: riesegui `./provision-db-user.sh` con la password corretta e verifica che `--mysql-user-host` corrisponda davvero all'host da cui si collega l'applicazione;
- errore di permessi insufficienti: verifica che l'utente applicativo sia dedicato all'app e che lo script abbia applicato i grant sul database corretto.

Rotazione o rimozione delle vecchie credenziali prevedibili:

- se mantieni lo stesso username applicativo, ruota la password aggiornando `ENGINE_GALLERY_DB_PASSWORD` e rieseguendo `./provision-db-user.sh`;
- se passi a un nuovo username dedicato, aggiorna prima la configurazione dell'applicazione, verifica la connessione e solo dopo rimuovi esplicitamente l'account legacy con un comando amministrativo MySQL.

Rollback operativo sicuro:

- ripristina la precedente configurazione esterna solo se ancora nota e controllata;
- in alternativa crea un nuovo account applicativo dedicato con `./provision-db-user.sh` e aggiorna l'applicazione senza ricreare database o tabelle.

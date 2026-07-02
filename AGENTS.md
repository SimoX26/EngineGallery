# Engine Gallery

## Panoramica
- Web app Java/Maven con packaging `war`, architettura MVC, Servlet/JSP e layer DAO.
- Il modulo Android vive in `android-app/` ed è separato dalla webapp principale.

## Struttura utile
- `src/main/java/it/SimoSW/controller/gui`: servlet, filter e listener della web UI.
- `src/main/java/it/SimoSW/controller/app`: logica applicativa.
- `src/main/java/it/SimoSW/model`: entità, enum e interfacce DAO.
- `src/main/java/it/SimoSW/model/dao/database`: implementazioni JDBC/MySQL.
- `src/main/java/it/SimoSW/util`: bootstrap, sicurezza, upload, navigazione e audit.
- `src/main/webapp/WEB-INF/views`: JSP.
- `src/main/webapp/assets`: CSS, JavaScript e immagini.
- `src/main/resources/db.sql` e `src/main/resources/migrations/`: schema e migrazioni SQL.
- `docs/`: documentazione tecnica.
- `build-apk.sh`, `deploy-remoto.sh`, `backup-remoto.sh`: script operativi nella root.

## Comandi supportati
- Da root: `mvn test`
- Da root: `mvn clean package`
- Da root: `./build-apk.sh`
- Da `android-app/`: `./gradlew assembleDebug`
- Da `android-app/`: `./gradlew assembleRelease`

## Configurazione e runtime
- La webapp usa Java 16 come target di compilazione.
- Il backend gira in un container Servlet 4.0/JSP 2.3.
- La configurazione DB è letta da `enginegallery.db.url`, `enginegallery.db.user`, `enginegallery.db.password` oppure da `ENGINE_GALLERY_DB_URL`, `ENGINE_GALLERY_DB_USER`, `ENGINE_GALLERY_DB_PASSWORD`.
- Il path upload è risolto tramite `enginegallery.upload.dir` oppure `ENGINE_GALLERY_UPLOAD_DIR`.
- Il secret per i token remember-me è letto da `enginegallery.remember.secret` / `enginegallery.remember.secret.file` oppure da `ENGINE_GALLERY_REMEMBER_SECRET` / `ENGINE_GALLERY_REMEMBER_SECRET_FILE`.

## Convenzioni
- Mantieni separati controller web, logica applicativa e persistenza.
- Le route web sono basate su annotazioni (`@WebServlet`, `@WebFilter`, `@WebListener`), quindi `web.xml` resta minimale.
- Non introdurre segreti, credenziali o valori sensibili in codice o documentazione.
- Non modificare `target/`, `backups/`, `android-app/app/build/` o `android-app/.gradle/` a mano.
- Tratta `deploy-remoto.sh` e `backup-remoto.sh` come operazioni manuali: eseguili solo su richiesta esplicita.

## Verifiche
- `mvn test` attualmente completa con successo.
- Non risultano test sorgente sotto `src/test` al momento.

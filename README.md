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

#### 📁 Percorso di salvataggio immagini

Per default le immagini vengono salvate in una cartella "umana" dentro la home utente:

```
~/EngineGallery/uploads/engines
```

Puoi personalizzare il percorso con:

- variabile ambiente `ENGINE_GALLERY_UPLOAD_DIR`
- system property `enginegallery.upload.dir`

## 📱 App Android con grafica identica

Per avere la stessa identica UI del progetto web, è stato aggiunto un contenitore Android in `android-app/` che usa `WebView` e carica direttamente l'app Engine Gallery.

### Come usare

1. Apri `android-app/` in Android Studio.
2. Imposta l'URL backend in `android-app/gradle.properties`:
   - Emulatore Android + Tomcat locale: `http://10.0.2.2:8080/EngineGallery/`
   - Dispositivo reale sulla stessa rete: `http://IP_DEL_PC:8080/EngineGallery/`
   - Server pubblico HTTPS: `https://tuo-dominio/EngineGallery/`
3. Builda ed esegui l'app Android.

### Note

- Upload file da WebView supportato (chooser Android).
- Navigazione indietro gestita con il tasto Back.
- `usesCleartextTraffic=true` è attivo per facilitare test in HTTP locale.

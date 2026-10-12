# Notifiche Android: motore consegnato

Quando un motore passa da uno stato diverso da `DELIVERED` a `DELIVERED`, il server invia una notifica FCM ai telefoni Android registrati dagli utenti che hanno **attualmente** il ruolo `ADMIN`. Un nuovo motore creato già consegnato e i salvataggi ripetuti di un motore già consegnato non generano notifiche.

## Configurazione necessaria

1. Creare un progetto Firebase e aggiungere un'app Android con package `it.simosw.enginegallery`.
2. Scaricare `google-services.json` in `android-app/app/`. Il file è ignorato da Git. Ricompilare e distribuire l'APK agli amministratori.
3. Generare un account di servizio per Firebase Admin SDK e conservare il file JSON **fuori dal repository**. Configurare il server con `enginegallery.firebase.credentials.file` oppure `ENGINE_GALLERY_FIREBASE_CREDENTIALS_FILE`, indicando il percorso assoluto del file.
4. Applicare `src/main/resources/migrations/2026-10-12_create_android_push_tokens.sql` al database esistente prima di usare la nuova versione del server.

La registrazione del telefono avviene alla visita di una pagina nell'app Android dopo l'accesso di un amministratore. Su Android 13 o successivo viene richiesto il permesso di notifica. Alla disconnessione vengono rimossi i token dell'utente; alla successiva apertura dell'app si registrano di nuovo. I token di utenti che non sono più amministratori vengono esclusi dalla query di invio.

Senza `google-services.json` l'APK compila, ma non registra dispositivi. Senza le credenziali server l'applicazione web funziona, ma non invia notifiche. Per verificare la consegna effettiva servono un progetto Firebase configurato, un telefono Android e un motore passato a `DELIVERED`.

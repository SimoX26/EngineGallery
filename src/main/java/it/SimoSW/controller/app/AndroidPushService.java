package it.SimoSW.controller.app;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import it.SimoSW.model.dao.database.DatabaseAndroidPushTokenDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidPushService implements DeliveredEngineNotifier {
    private static final Logger LOGGER = Logger.getLogger(AndroidPushService.class.getName());
    private final DatabaseAndroidPushTokenDAO tokenDAO;
    private final ExecutorService sender = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "enginegallery-android-push");
        thread.setDaemon(true);
        return thread;
    });
    private final FirebaseApp firebaseApp;

    public AndroidPushService(DatabaseAndroidPushTokenDAO tokenDAO) {
        this.tokenDAO = tokenDAO;
        String credentialsPath = System.getProperty("enginegallery.firebase.credentials.file");
        if (credentialsPath == null || credentialsPath.isBlank()) {
            credentialsPath = System.getenv("ENGINE_GALLERY_FIREBASE_CREDENTIALS_FILE");
        }
        if (credentialsPath == null || credentialsPath.isBlank()) {
            firebaseApp = null;
            LOGGER.info("Notifiche Android non configurate: manca il file credenziali Firebase");
            return;
        }
        FirebaseApp app;
        try (var input = Files.newInputStream(Path.of(credentialsPath))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(input))
                    .build();
            app = FirebaseApp.initializeApp(options, "enginegallery-android-push");
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Configurazione Firebase Android non valida: notifiche disabilitate", ex);
            app = null;
        }
        firebaseApp = app;
    }

    public void register(long userId, String token) {
        tokenDAO.register(userId, token);
    }

    public void deleteToken(String token) {
        tokenDAO.deleteToken(token);
    }

    public void deleteUserTokens(long userId) {
        tokenDAO.deleteUserTokens(userId);
    }

    @Override
    public void notifyDelivered(String engineRef) {
        if (firebaseApp == null) {
            return;
        }
        sender.execute(() -> {
            try {
                for (String token : tokenDAO.findAdminTokens()) {
                    Message message = Message.builder()
                            .setToken(token)
                            .setNotification(Notification.builder()
                                    .setTitle("Motore consegnato")
                                    .setBody("Il motore " + engineRef + " è stato consegnato")
                                    .build())
                            .putData("engineRef", engineRef)
                            .setAndroidConfig(AndroidConfig.builder()
                                    .setNotification(AndroidNotification.builder()
                                            .setChannelId("delivered_engines")
                                            .build())
                                    .build())
                            .build();
                    try {
                        FirebaseMessaging.getInstance(firebaseApp).send(message);
                    } catch (FirebaseMessagingException ex) {
                        if (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                            tokenDAO.deleteToken(token);
                        } else {
                            LOGGER.log(Level.WARNING, "Invio notifica Android fallito", ex);
                        }
                    }
                }
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Preparazione notifiche Android fallita", ex);
            }
        });
    }

    public void close() {
        sender.shutdown();
        if (firebaseApp != null) {
            firebaseApp.delete();
        }
    }
}

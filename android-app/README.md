de# Engine Gallery Android

Wrapper Android installabile per la webapp Engine Gallery.

## Requisiti

- Android Studio (consigliato) oppure JDK 17 + Android SDK
- Dispositivo Android o emulatore
- Backend EngineGallery raggiungibile via rete

## Configurazione URL backend

L'app legge l'URL da proprietà Gradle `ENGINE_GALLERY_BASE_URL`.

Esempio build debug con backend in LAN:

```bash
./gradlew assembleDebug -PENGINE_GALLERY_BASE_URL=http://192.168.1.50:8080/EngineGallery
```

Note importanti:
- Su dispositivo fisico non usare `localhost`, ma l'IP della macchina che ospita Tomcat.
- Assicurati che telefono e server siano sulla stessa rete o raggiungibili via internet/VPN.

## Build APK

Debug APK:

```bash
./gradlew assembleDebug
```

Output:

`app/build/outputs/apk/debug/app-debug.apk`

Release APK:

```bash
./gradlew assembleRelease
```

Output:

`app/build/outputs/apk/release/app-release-unsigned.apk`

## Installazione su dispositivo

Con ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Cosa include il wrapper

- WebView con JavaScript e storage abilitati
- Pull-to-refresh
- Navigazione indietro gestita come app nativa
- Upload file e foto da camera (FileProvider)
- Supporto anche a backend HTTP in LAN

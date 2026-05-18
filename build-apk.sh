#!/usr/bin/env bash
set -euo pipefail

ANDROID_URL_DEFAULT="https://rettificamotorilacroce.it"

usage() {
  cat <<'HELP'
Uso:
  ./build-apk.sh [opzioni]

Esempi:
  ./build-apk.sh
  ./build-apk.sh --android-url https://rettificamotorilacroce.it

Opzioni:
  --android-url URL backend da iniettare nella build Android (ENGINE_GALLERY_BASE_URL)
  --help, -h    Mostra questo aiuto
HELP
}

ANDROID_URL="$ANDROID_URL_DEFAULT"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --android-url)
      ANDROID_URL="${2:-}"
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Argomento non riconosciuto: $1" >&2
      usage
      exit 1
      ;;
  esac
done

ANDROID_DIR="android-app"
APK_DIR="${ANDROID_DIR}/app/build/outputs/apk/debug"

if [[ ! -d "$ANDROID_DIR" ]]; then
  echo "Errore: cartella Android non trovata: $ANDROID_DIR" >&2
  exit 1
fi

GRADLE_CMD=("./gradlew" "assembleDebug")
if [[ -n "$ANDROID_URL" ]]; then
  GRADLE_CMD+=("-PENGINE_GALLERY_BASE_URL=${ANDROID_URL}")
fi

echo ">> Android backend URL: ${ANDROID_URL}"
echo ">> Build Android in ${ANDROID_DIR}: ${GRADLE_CMD[*]}"
(
  cd "$ANDROID_DIR"
  "${GRADLE_CMD[@]}"
)

APK_FILE="$(ls -t "${APK_DIR}"/*.apk 2>/dev/null | head -n 1 || true)"
if [[ -z "$APK_FILE" || ! -f "$APK_FILE" ]]; then
  echo "Errore: APK non trovato dopo la build in: $APK_DIR" >&2
  exit 1
fi

APK_ABS_PATH="$(cd "$(dirname "$APK_FILE")" && pwd)/$(basename "$APK_FILE")"

echo ">> APK generato: $APK_FILE"
echo ">> Percorso filesystem: $APK_ABS_PATH"

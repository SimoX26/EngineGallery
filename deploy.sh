#!/usr/bin/env bash
set -euo pipefail

# Default locali/remoti richiesti
LOCAL_WEBAPPS_DEFAULT="/home/simone/apache-tomcat-9.0.112/webapps"
REMOTE_USER_DEFAULT="root"
REMOTE_HOST_DEFAULT="82.165.20.124"
REMOTE_WEBAPPS_DEFAULT="/opt/tomcat/webapps"
REMOTE_PASSWORD_DEFAULT="F1yKNvqwl6qw8ED"
ANDROID_URL_DEFAULT="http://${REMOTE_HOST_DEFAULT}:8080/EngineGallery"

usage() {
  cat <<'HELP'
Uso:
  ./deploy.sh (--locale | --remoto | --target <destinazione_scp> | --android | --apk) [opzioni]

Esempi:
  ./deploy.sh --locale
  ./deploy.sh --remoto
  ./deploy.sh --remoto --remote-path ~/webapps
  ./deploy.sh --remoto --remote-sql-path ~/sql
  ./deploy.sh --target user@192.168.1.50:~/webapps/
  ./deploy.sh --android
  ./deploy.sh --apk
  ./deploy.sh --android --android-url http://82.165.20.124:8080/EngineGallery

Opzioni:
  --locale      Copia il WAR in locale su /home/simone/apache-tomcat-9.0.112/webapps
  --remoto      Copia il WAR su host remoto preconfigurato via scp
  --target      Destinazione SCP custom nel formato [user@]host:/path/
  --android     Build APK Android (android-app) e installa via adb se disponibile
  --apk         Build APK Android (android-app) senza installazione adb
  --android-url URL backend da iniettare nella build Android (ENGINE_GALLERY_BASE_URL)
  --android-no-install Salta installazione adb automatica (build-only)
  --port        Porta SSH (default: 22)
  --password    Password SSH (default remoto preconfigurato)
  --remote-user Utente SSH remoto (default: root)
  --remote-host Host SSH remoto (default: 82.165.20.124)
  --remote-path Cartella remota webapps (default: ~/webapps)
  --remote-sql-path Cartella remota script SQL (default: ~)
  --local-path  Cartella locale webapps (default: /home/simone/apache-tomcat-9.0.112/webapps)
  --skip-build  Salta 'mvn clean package' e usa WAR già presente in target/
  --help        Mostra questo aiuto

Note:
  - Lo script cerca il WAR in target/ (es. target/EngineGallery.war).
  - Se ci sono più WAR, usa il più recente.
HELP
}

TARGET=""
PORT="22"
PASSWORD="$REMOTE_PASSWORD_DEFAULT"
LOCAL_PATH="$LOCAL_WEBAPPS_DEFAULT"
REMOTE_USER="$REMOTE_USER_DEFAULT"
REMOTE_HOST="$REMOTE_HOST_DEFAULT"
REMOTE_PATH="$REMOTE_WEBAPPS_DEFAULT"
REMOTE_SQL_PATH=""
SKIP_BUILD="false"
ANDROID_URL="$ANDROID_URL_DEFAULT"
ANDROID_URL_EXPLICIT="false"
ANDROID_INSTALL="true"
MODE=""

resolve_ipv4() {
  local host="$1"
  local ip=""

  if [[ "$host" == "localhost" || "$host" == "127.0.0.1" ]]; then
    ip="$(hostname -I 2>/dev/null | awk '{print $1}' || true)"
    if [[ -z "$ip" ]]; then
      ip="127.0.0.1"
    fi
    echo "$ip"
    return 0
  fi

  ip="$(getent ahostsv4 "$host" 2>/dev/null | awk '{print $1; exit}' || true)"
  if [[ -z "$ip" ]]; then
    ip="$host"
  fi
  echo "$ip"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --locale)
      MODE="locale"
      shift
      ;;
    --remoto)
      MODE="remoto"
      shift
      ;;
    --target)
      TARGET="${2:-}"
      MODE="target"
      shift 2
      ;;
    --android)
      MODE="android"
      shift
      ;;
    --apk)
      MODE="apk"
      ANDROID_INSTALL="false"
      shift
      ;;
    --android-url)
      ANDROID_URL="${2:-}"
      ANDROID_URL_EXPLICIT="true"
      shift 2
      ;;
    --android-no-install)
      ANDROID_INSTALL="false"
      shift
      ;;
    --port)
      PORT="${2:-}"
      shift 2
      ;;
    --password)
      PASSWORD="${2:-}"
      shift 2
      ;;
    --remote-user)
      REMOTE_USER="${2:-}"
      shift 2
      ;;
    --remote-host)
      REMOTE_HOST="${2:-}"
      shift 2
      ;;
    --remote-path)
      REMOTE_PATH="${2:-}"
      shift 2
      ;;
    --remote-sql-path)
      REMOTE_SQL_PATH="${2:-}"
      shift 2
      ;;
    --local-path)
      LOCAL_PATH="${2:-}"
      shift 2
      ;;
    --skip-build)
      SKIP_BUILD="true"
      shift
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

if [[ -z "$MODE" ]]; then
  echo "Errore: devi specificare --locale, --remoto, --target, --android o --apk." >&2
  usage
  exit 1
fi

if [[ "$ANDROID_URL_EXPLICIT" != "true" ]]; then
  ANDROID_URL="http://${REMOTE_HOST}:8080/EngineGallery"
fi

if [[ "$MODE" == "target" ]] && ! [[ "$TARGET" =~ :/ ]]; then
  echo "Errore: --target deve essere nel formato [user@]host:/path/." >&2
  exit 1
fi

if [[ "$MODE" == "android" || "$MODE" == "apk" ]]; then
  ANDROID_DIR="android-app"
  APK_DIR="${ANDROID_DIR}/app/build/outputs/apk/debug"
  PACKAGE_NAME="it.simosw.enginegallery"

  if [[ ! -d "$ANDROID_DIR" ]]; then
    echo "Errore: cartella Android non trovata: $ANDROID_DIR" >&2
    exit 1
  fi

  GRADLE_CMD=("./gradlew" "assembleDebug")
  if [[ -n "$ANDROID_URL" ]]; then
    GRADLE_CMD+=("-PENGINE_GALLERY_BASE_URL=${ANDROID_URL}")
  fi
  echo ">> Android backend URL: ${ANDROID_URL}"

  if [[ "$MODE" == "apk" ]]; then
    BUILD_LOG="$(mktemp)"
    if ! (
      cd "$ANDROID_DIR"
      "${GRADLE_CMD[@]}" >"$BUILD_LOG" 2>&1
    ); then
      cat "$BUILD_LOG" >&2
      rm -f "$BUILD_LOG"
      exit 1
    fi
    rm -f "$BUILD_LOG"
  else
    echo ">> Build Android in ${ANDROID_DIR}: ${GRADLE_CMD[*]}"
    (
      cd "$ANDROID_DIR"
      "${GRADLE_CMD[@]}"
    )
  fi

  APK_FILE="$(ls -t "${APK_DIR}"/*.apk 2>/dev/null | head -n 1 || true)"
  if [[ -z "$APK_FILE" || ! -f "$APK_FILE" ]]; then
    echo "Errore: APK non trovato dopo la build in: $APK_DIR" >&2
    exit 1
  fi

  APK_ABS_PATH="$(cd "$(dirname "$APK_FILE")" && pwd)/$(basename "$APK_FILE")"
  APK_DIR_ABS_PATH="$(cd "$APK_DIR" && pwd)"

  if [[ "$ANDROID_INSTALL" == "false" ]]; then
    if [[ "$MODE" == "apk" ]]; then
      echo "$APK_DIR_ABS_PATH"
    else
      echo ">> APK generato: $APK_FILE"
      echo ">> Percorso filesystem: $APK_ABS_PATH"
      echo ">> Link file: file://$APK_ABS_PATH"
      echo ">> Installazione adb saltata (--android-no-install)."
    fi
    exit 0
  fi

  echo ">> APK generato: $APK_FILE"
  echo ">> Percorso filesystem: $APK_ABS_PATH"
  echo ">> Link file: file://$APK_ABS_PATH"

  if ! command -v adb >/dev/null 2>&1; then
    echo ">> adb non trovato: installazione automatica saltata."
    echo ">> Installa manualmente con:"
    echo "   adb install -r $APK_FILE"
    exit 0
  fi

  ADB_DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" {count++} END {print count+0}')"
  if [[ "$ADB_DEVICE_COUNT" -eq 0 ]]; then
    echo ">> Nessun dispositivo adb collegato: installazione automatica saltata."
    echo ">> Installa manualmente con:"
    echo "   adb install -r $APK_FILE"
    exit 0
  fi

  echo ">> Installazione APK su dispositivo (adb install -r)"
  adb install -r "$APK_FILE"
  echo ">> Deploy Android completato."
  echo ">> Pacchetto installato: ${PACKAGE_NAME}"
  exit 0
fi

if [[ "$SKIP_BUILD" != "true" ]]; then
  echo ">> Eseguo build Maven: mvn clean package"
  mvn clean package
fi

WAR_FILE="$(ls -t target/*.war 2>/dev/null | head -n 1 || true)"
if [[ -z "$WAR_FILE" ]]; then
  echo "Errore: nessun file WAR trovato in target/." >&2
  exit 1
fi
WAR_NAME="$(basename "$WAR_FILE")"
APP_CONTEXT="${WAR_NAME%.war}"

echo ">> WAR selezionato: $WAR_FILE"

if [[ "$MODE" == "locale" ]]; then
  if [[ ! -d "$LOCAL_PATH" ]]; then
    echo "Errore: cartella locale non trovata: $LOCAL_PATH" >&2
    exit 1
  fi
  echo ">> Deploy locale in: $LOCAL_PATH"
  cp -f "$WAR_FILE" "$LOCAL_PATH/"
  echo ">> Deploy locale completato."

  LOCAL_IP="$(resolve_ipv4 "localhost")"
  if [[ "$APP_CONTEXT" == "ROOT" ]]; then
    APP_URL="http://${LOCAL_IP}:8080/"
  else
    APP_URL="http://${LOCAL_IP}:8080/${APP_CONTEXT}/"
  fi
  echo ">> Avvio applicativo: $APP_URL"
  exit 0
fi

if [[ "$MODE" == "remoto" ]]; then
  TARGET="${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH%/}/"
fi

if ! command -v sshpass >/dev/null 2>&1; then
  echo "Errore: 'sshpass' non trovato." >&2
  echo "Installa sshpass per usare il deploy automatico con password." >&2
  exit 1
fi
SCP_CMD=(sshpass -p "$PASSWORD" scp -P "$PORT")

SCP_CMD+=("$WAR_FILE" "$TARGET")

echo ">> Upload SCP verso: $TARGET"
"${SCP_CMD[@]}"

if [[ "$MODE" == "remoto" ]]; then
  SQL_BASE_PATH="${REMOTE_SQL_PATH:-~}"
  mapfile -d '' SQL_FILES < <(find src/main/resources -type f -name "*.sql" -print0 | sort -z)

  if [[ "${#SQL_FILES[@]}" -gt 0 ]]; then
    SCP_SQL_CMD=(sshpass -p "$PASSWORD" scp -P "$PORT")

    echo ">> Upload script SQL in: ${REMOTE_USER}@${REMOTE_HOST}:${SQL_BASE_PATH}"
    for sql_file in "${SQL_FILES[@]}"; do
      "${SCP_SQL_CMD[@]}" "$sql_file" "${REMOTE_USER}@${REMOTE_HOST}:${SQL_BASE_PATH}/"
    done
  else
    echo ">> Nessuno script SQL trovato in src/main/resources."
  fi
fi

echo ">> Deploy completato con successo."

if [[ "$MODE" == "remoto" ]]; then
  REMOTE_IP="$(resolve_ipv4 "$REMOTE_HOST")"
  if [[ "$APP_CONTEXT" == "ROOT" ]]; then
    APP_URL="http://${REMOTE_IP}:8080/"
  else
    APP_URL="http://${REMOTE_IP}:8080/${APP_CONTEXT}/"
  fi
  echo ">> Avvio applicativo: $APP_URL"
fi

if [[ "$MODE" == "target" ]]; then
  TARGET_HOST="$(echo "$TARGET" | sed -E 's|^([^@]+@)?([^:]+):.*$|\2|')"
  TARGET_IP="$(resolve_ipv4 "$TARGET_HOST")"
  if [[ "$APP_CONTEXT" == "ROOT" ]]; then
    APP_URL="http://${TARGET_IP}:8080/"
  else
    APP_URL="http://${TARGET_IP}:8080/${APP_CONTEXT}/"
  fi
  echo ">> Avvio applicativo (stimato): $APP_URL"
fi

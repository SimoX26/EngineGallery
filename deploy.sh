#!/usr/bin/env bash
set -euo pipefail

# Default locali/remoti richiesti
LOCAL_WEBAPPS_DEFAULT="/home/simone/apache-tomcat-9.0.112/webapps"
REMOTE_USER_DEFAULT="admin"
REMOTE_HOST_DEFAULT="ec2-13-62-51-239.eu-north-1.compute.amazonaws.com"
REMOTE_WEBAPPS_DEFAULT="~"
REMOTE_KEY_DEFAULT="/home/simone/Documenti/Keys.pem"

usage() {
  cat <<'HELP'
Uso:
  ./deploy.sh (--locale | --remoto | --target <destinazione_scp>) [opzioni]

Esempi:
  ./deploy.sh --locale
  ./deploy.sh --remoto
  ./deploy.sh --remoto --remote-path ~/webapps
  ./deploy.sh --remoto --remote-sql-path ~/sql
  ./deploy.sh --target user@192.168.1.50:~/webapps/

Opzioni:
  --locale      Copia il WAR in locale su /home/simone/apache-tomcat-9.0.112/webapps
  --remoto      Copia il WAR su host remoto preconfigurato via scp
  --target      Destinazione SCP custom nel formato [user@]host:/path/
  --port        Porta SSH (default: 22)
  --identity    Chiave SSH (default remoto: /home/simone/Documenti/Keys.pem)
  --remote-user Utente SSH remoto (default: admin)
  --remote-host Host SSH remoto (default: ec2-13-62-51-239.eu-north-1.compute.amazonaws.com)
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
IDENTITY="$REMOTE_KEY_DEFAULT"
LOCAL_PATH="$LOCAL_WEBAPPS_DEFAULT"
REMOTE_USER="$REMOTE_USER_DEFAULT"
REMOTE_HOST="$REMOTE_HOST_DEFAULT"
REMOTE_PATH="$REMOTE_WEBAPPS_DEFAULT"
REMOTE_SQL_PATH=""
SKIP_BUILD="false"
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
    --port)
      PORT="${2:-}"
      shift 2
      ;;
    --identity)
      IDENTITY="${2:-}"
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
  echo "Errore: devi specificare --locale, --remoto o --target." >&2
  usage
  exit 1
fi

if [[ "$MODE" == "target" ]] && ! [[ "$TARGET" =~ :/ ]]; then
  echo "Errore: --target deve essere nel formato [user@]host:/path/." >&2
  exit 1
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

# Correzione automatica typo comune: "7home/..." -> "/home/..."
if [[ "$IDENTITY" == 7home/* ]]; then
  IDENTITY="/${IDENTITY}"
fi

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

SCP_CMD=(scp -P "$PORT")
if [[ -n "$IDENTITY" ]]; then
  SCP_CMD+=(-i "$IDENTITY")
fi

SCP_CMD+=("$WAR_FILE" "$TARGET")

echo ">> Upload SCP verso: $TARGET"
"${SCP_CMD[@]}"

if [[ "$MODE" == "remoto" ]]; then
  SQL_BASE_PATH="${REMOTE_SQL_PATH:-~}"
  mapfile -d '' SQL_FILES < <(find src/main/resources -type f -name "*.sql" -print0 | sort -z)

  if [[ "${#SQL_FILES[@]}" -gt 0 ]]; then
    SCP_SQL_CMD=(scp -P "$PORT")
    if [[ -n "$IDENTITY" ]]; then
      SCP_SQL_CMD+=(-i "$IDENTITY")
    fi

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

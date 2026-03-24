#!/usr/bin/env bash
set -euo pipefail

# Default locali/remoti richiesti
LOCAL_WEBAPPS_DEFAULT="/home/simone/apache-tomcat-9.0.112/webapps"
REMOTE_USER_DEFAULT="admin"
REMOTE_HOST_DEFAULT="ec2-13-62-51-239.eu-north-1.compute.amazonaws.com"
REMOTE_WEBAPPS_DEFAULT="/opt/tomcat/webapps"
REMOTE_KEY_DEFAULT="/home/simone/Documenti/Keys.pem"

usage() {
  cat <<'HELP'
Uso:
  ./deploy.sh (--locale | --remoto | --target <destinazione_scp>) [opzioni]

Esempi:
  ./deploy.sh --locale
  ./deploy.sh --remoto
  ./deploy.sh --remoto --remote-path /var/lib/tomcat9/webapps
  ./deploy.sh --target user@192.168.1.50:/opt/tomcat/webapps/

Opzioni:
  --locale      Copia il WAR in locale su /home/simone/apache-tomcat-9.0.112/webapps
  --remoto      Copia il WAR su host remoto preconfigurato via scp
  --target      Destinazione SCP custom nel formato [user@]host:/path/
  --port        Porta SSH (default: 22)
  --identity    Chiave SSH (default remoto: /home/simone/Documenti/Keys.pem)
  --remote-user Utente SSH remoto (default: admin)
  --remote-host Host SSH remoto (default: ec2-13-62-51-239.eu-north-1.compute.amazonaws.com)
  --remote-path Cartella remota webapps (default: /opt/tomcat/webapps)
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
SKIP_BUILD="false"
MODE=""

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

echo ">> Deploy completato con successo."

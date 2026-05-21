#!/usr/bin/env bash
set -euo pipefail

REMOTE_USER_DEFAULT="root"
REMOTE_HOST_DEFAULT="82.165.20.124"
REMOTE_PASSWORD_DEFAULT="F1yKNvqwl6qw8ED"
REMOTE_MEDIA_PATH_DEFAULT="/opt/tomcat/webapps"
LOCAL_BACKUP_PATH_DEFAULT="backups"

usage() {
  cat <<'HELP'
Uso:
  ./backup-remoto.sh [opzioni]

Esempi:
  ./backup-remoto.sh --db-name enginegallery --db-user root --db-password segreta
  ./backup-remoto.sh --port 2222 --remote-media-path /opt/tomcat/webapps/media \
    --db-name enginegallery --db-user app --db-password segreta

Opzioni:
  --port               Porta SSH (default: 22)
  --password           Password SSH (default remoto preconfigurato)
  --remote-user        Utente SSH remoto (default: root)
  --remote-host        Host SSH remoto (default: 82.165.20.124)
  --db-name            Nome database remoto (obbligatorio)
  --db-user            Utente database remoto (obbligatorio)
  --db-password        Password database remoto (obbligatorio)
  --remote-media-path  Cartella media remota da copiare (default: /opt/tomcat/webapps)
  --local-backup-path  Cartella base locale backup (default: backups)
  --help, -h           Mostra questo aiuto

Note:
  - Lo script non modifica nulla sul server remoto.
  - Dump SQL eseguito via SSH e salvato in locale.
  - Copia media via rsync se disponibile, altrimenti scp -r.
HELP
}

require_value() {
  local opt="$1"
  local val="${2:-}"
  if [[ -z "$val" || "$val" == --* ]]; then
    echo "Errore: valore mancante per $opt" >&2
    usage
    exit 1
  fi
}

squote() {
  printf "'%s'" "${1//\'/\'\"\'\"\'}"
}

PORT="22"
PASSWORD="$REMOTE_PASSWORD_DEFAULT"
REMOTE_USER="$REMOTE_USER_DEFAULT"
REMOTE_HOST="$REMOTE_HOST_DEFAULT"
DB_NAME=""
DB_USER=""
DB_PASSWORD=""
REMOTE_MEDIA_PATH="$REMOTE_MEDIA_PATH_DEFAULT"
LOCAL_BACKUP_PATH="$LOCAL_BACKUP_PATH_DEFAULT"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --port)
      require_value "$1" "${2:-}"
      PORT="$2"
      shift 2
      ;;
    --password)
      require_value "$1" "${2:-}"
      PASSWORD="$2"
      shift 2
      ;;
    --remote-user)
      require_value "$1" "${2:-}"
      REMOTE_USER="$2"
      shift 2
      ;;
    --remote-host)
      require_value "$1" "${2:-}"
      REMOTE_HOST="$2"
      shift 2
      ;;
    --db-name)
      require_value "$1" "${2:-}"
      DB_NAME="$2"
      shift 2
      ;;
    --db-user)
      require_value "$1" "${2:-}"
      DB_USER="$2"
      shift 2
      ;;
    --db-password)
      require_value "$1" "${2:-}"
      DB_PASSWORD="$2"
      shift 2
      ;;
    --remote-media-path)
      require_value "$1" "${2:-}"
      REMOTE_MEDIA_PATH="$2"
      shift 2
      ;;
    --local-backup-path)
      require_value "$1" "${2:-}"
      LOCAL_BACKUP_PATH="$2"
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

if [[ -z "$DB_NAME" || -z "$DB_USER" || -z "$DB_PASSWORD" ]]; then
  echo "Errore: --db-name, --db-user e --db-password sono obbligatori." >&2
  usage
  exit 1
fi

if ! command -v sshpass >/dev/null 2>&1; then
  echo "Errore: 'sshpass' non trovato." >&2
  echo "Installa sshpass per usare il backup remoto con password." >&2
  exit 1
fi

RSYNC_AVAILABLE="false"
if command -v rsync >/dev/null 2>&1; then
  RSYNC_AVAILABLE="true"
fi

TARGET_SSH="${REMOTE_USER}@${REMOTE_HOST}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BASE_DIR="${LOCAL_BACKUP_PATH%/}"
RUN_DIR="${BASE_DIR}/backup-${TIMESTAMP}"
DB_DUMP_FILE="${RUN_DIR}/backup-db-${TIMESTAMP}.sql"
MEDIA_DIR="${RUN_DIR}/backup-media-${TIMESTAMP}"

echo ">> Creo struttura backup locale in: ${RUN_DIR}"
mkdir -p "$RUN_DIR" "$MEDIA_DIR"

echo ">> Verifico accesso remoto e cartella media: ${REMOTE_MEDIA_PATH}"
sshpass -p "$PASSWORD" ssh -p "$PORT" "$TARGET_SSH" "test -d $(squote "$REMOTE_MEDIA_PATH")"

echo ">> Eseguo dump database remoto: ${DB_NAME}"
REMOTE_DUMP_CMD="MYSQL_PWD=$(squote "$DB_PASSWORD") mysqldump -u $(squote "$DB_USER") --single-transaction --quick --routines --events $(squote "$DB_NAME")"
sshpass -p "$PASSWORD" ssh -p "$PORT" "$TARGET_SSH" "$REMOTE_DUMP_CMD" > "$DB_DUMP_FILE"

echo ">> Dump SQL salvato in: ${DB_DUMP_FILE}"

echo ">> Copia media remoti in: ${MEDIA_DIR}"
if [[ "$RSYNC_AVAILABLE" == "true" ]]; then
  echo ">> Uso rsync per la copia media"
  sshpass -p "$PASSWORD" rsync -az \
    -e "ssh -p ${PORT} -o StrictHostKeyChecking=no" \
    "${TARGET_SSH}:${REMOTE_MEDIA_PATH%/}/" \
    "${MEDIA_DIR}/"
else
  echo ">> rsync non disponibile: uso fallback scp -r"
  sshpass -p "$PASSWORD" scp -P "$PORT" -r \
    "${TARGET_SSH}:${REMOTE_MEDIA_PATH%/}/." \
    "$MEDIA_DIR/"
fi

echo ">> Backup completato con successo."
echo ">> Cartella backup: ${RUN_DIR}"

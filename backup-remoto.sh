#!/usr/bin/env bash
set -euo pipefail

REMOTE_USER_DEFAULT="root"
REMOTE_HOST_DEFAULT="82.165.20.124"
REMOTE_PORT_DEFAULT="22"
DB_NAME_DEFAULT="engine_gallery"
DB_USER_DEFAULT="root"
LOCAL_BACKUP_PATH_DEFAULT="backups"
UPLOAD_ROOT_DEFAULT="/var/lib/EngineGallery/uploads"

usage() {
  cat <<'HELP'
Uso:
  ./backup-remoto.sh [opzioni]

Esempi:
  ./backup-remoto.sh
  ./backup-remoto.sh --password 'PASSWORD_SSH'

Opzioni:
  --port               Porta SSH (default: 22, da costante)
  --password           Password SSH (se assente viene chiesta in input nascosto)
  --remote-user        Utente SSH remoto (default: root)
  --remote-host        Host SSH remoto (default: 82.165.20.124)
  --db-name            Nome database remoto (default da costante script)
  --db-user            Utente database remoto (default da costante script)
  --local-backup-path  Cartella base locale backup (default: backups)
  --help, -h           Mostra questo aiuto

Note:
  - Il path media/upload remoto viene rilevato automaticamente dal progetto.
  - Rilevamento upload root: enginegallery.upload.dir / ENGINE_GALLERY_UPLOAD_DIR, altrimenti default applicativo.
  - Lo script non modifica nulla sul server remoto.
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

PORT="$REMOTE_PORT_DEFAULT"
PASSWORD=""
REMOTE_USER="$REMOTE_USER_DEFAULT"
REMOTE_HOST="$REMOTE_HOST_DEFAULT"
DB_NAME="$DB_NAME_DEFAULT"
DB_USER="$DB_USER_DEFAULT"
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

if ! command -v sshpass >/dev/null 2>&1; then
  echo "Errore: 'sshpass' non trovato." >&2
  echo "Installa sshpass per usare il backup remoto con password." >&2
  exit 1
fi

RSYNC_AVAILABLE="false"
if command -v rsync >/dev/null 2>&1; then
  RSYNC_AVAILABLE="true"
fi

if [[ -z "$PASSWORD" ]]; then
  read -r -s -p "Inserisci password SSH per ${REMOTE_USER}@${REMOTE_HOST}: " PASSWORD
  echo
fi

if [[ -z "$PASSWORD" ]]; then
  echo "Errore: password SSH non fornita." >&2
  exit 1
fi

TARGET_SSH="${REMOTE_USER}@${REMOTE_HOST}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BASE_DIR="${LOCAL_BACKUP_PATH%/}"
RUN_DIR="${BASE_DIR}/backup-${TIMESTAMP}"
DB_DUMP_FILE="${RUN_DIR}/backup-db-${TIMESTAMP}.sql"
MEDIA_DIR="${RUN_DIR}/backup-media-${TIMESTAMP}"

echo ">> Creo struttura backup locale in: ${RUN_DIR}"
mkdir -p "$RUN_DIR" "$MEDIA_DIR"

echo ">> Rilevo automaticamente upload/media root remoto"
UPLOAD_ROOT_REMOTE="$(sshpass -p "$PASSWORD" ssh -p "$PORT" "$TARGET_SSH" 'bash -s' <<'REMOTE'
set -euo pipefail

normalize_root() {
  local path="$1"
  case "${path##*/}" in
    engines|hydraulic|warehouse)
      dirname "$path"
      ;;
    *)
      echo "$path"
      ;;
  esac
}

from_env="${ENGINE_GALLERY_UPLOAD_DIR:-}"
from_prop=""

if [[ -z "$from_env" ]]; then
  tomcat_pid="$(pgrep -f 'org.apache.catalina.startup.Bootstrap' | head -n 1 || true)"
  if [[ -n "$tomcat_pid" && -r "/proc/${tomcat_pid}/cmdline" ]]; then
    from_prop="$(tr '\000' '\n' < "/proc/${tomcat_pid}/cmdline" | sed -n 's/^-Denginegallery\.upload\.dir=//p' | head -n 1 || true)"
  fi
fi

if [[ -n "$from_env" ]]; then
  normalize_root "$from_env"
  exit 0
fi

if [[ -n "$from_prop" ]]; then
  normalize_root "$from_prop"
  exit 0
fi

echo "/var/lib/EngineGallery/uploads"
REMOTE
)"

if [[ -z "$UPLOAD_ROOT_REMOTE" ]]; then
  echo "Errore: impossibile determinare il percorso upload remoto." >&2
  exit 1
fi

echo ">> Upload/media root remoto: ${UPLOAD_ROOT_REMOTE}"

sshpass -p "$PASSWORD" ssh -p "$PORT" "$TARGET_SSH" "test -d $(squote "$UPLOAD_ROOT_REMOTE")"

echo ">> Eseguo dump database remoto: ${DB_NAME}"
sshpass -p "$PASSWORD" ssh -p "$PORT" "$TARGET_SSH" \
  "DB_NAME_OVERRIDE=$(squote "$DB_NAME") DB_USER_OVERRIDE=$(squote "$DB_USER") bash -s" > "$DB_DUMP_FILE" <<'REMOTE'
set -euo pipefail

tomcat_pid="$(pgrep -f 'org.apache.catalina.startup.Bootstrap' | head -n 1 || true)"
cmdline=""
proc_env=""

if [[ -n "$tomcat_pid" ]]; then
  if [[ -r "/proc/${tomcat_pid}/cmdline" ]]; then
    cmdline="$(tr '\000' '\n' < "/proc/${tomcat_pid}/cmdline" || true)"
  fi
  if [[ -r "/proc/${tomcat_pid}/environ" ]]; then
    proc_env="$(tr '\000' '\n' < "/proc/${tomcat_pid}/environ" || true)"
  fi
fi

extract_prop() {
  local key="$1"
  if [[ -n "$cmdline" ]]; then
    printf '%s\n' "$cmdline" | sed -n "s/^-D${key}=//p" | head -n 1
  fi
}

extract_env() {
  local key="$1"
  if [[ -n "$proc_env" ]]; then
    printf '%s\n' "$proc_env" | sed -n "s/^${key}=//p" | head -n 1
  fi
}

DB_URL="$(extract_prop 'enginegallery\.db\.url')"
DB_USER_FROM_PROP="$(extract_prop 'enginegallery\.db\.user')"
DB_PASS_FROM_PROP="$(extract_prop 'enginegallery\.db\.password')"

if [[ -z "$DB_URL" ]]; then
  DB_URL="$(extract_env 'ENGINE_GALLERY_DB_URL')"
fi
if [[ -z "$DB_USER_FROM_PROP" ]]; then
  DB_USER_FROM_PROP="$(extract_env 'ENGINE_GALLERY_DB_USER')"
fi
if [[ -z "$DB_PASS_FROM_PROP" ]]; then
  DB_PASS_FROM_PROP="$(extract_env 'ENGINE_GALLERY_DB_PASSWORD')"
fi

DB_NAME_EFFECTIVE="${DB_NAME_OVERRIDE:-}"
DB_USER_EFFECTIVE="${DB_USER_OVERRIDE:-}"

if [[ -z "$DB_NAME_EFFECTIVE" && -n "$DB_URL" ]]; then
  DB_NAME_EFFECTIVE="$(printf '%s' "$DB_URL" | sed -E 's#^[^:]+://[^/]+/([^?]+).*$#\1#')"
fi
if [[ -z "$DB_NAME_EFFECTIVE" ]]; then
  DB_NAME_EFFECTIVE="engine_gallery"
fi

if [[ -z "$DB_USER_EFFECTIVE" ]]; then
  DB_USER_EFFECTIVE="root"
fi

# Preferisci dump locale via socket con utente root (tipico auth_socket su server Linux)
if mysqldump -u "$DB_USER_EFFECTIVE" \
  --single-transaction --quick --routines --events "$DB_NAME_EFFECTIVE" >/dev/null 2>&1; then
  mysqldump -u "$DB_USER_EFFECTIVE" \
    --single-transaction --quick --routines --events "$DB_NAME_EFFECTIVE"
  exit 0
fi

# Se root via socket non basta, prova con sudo (senza password prompt)
if command -v sudo >/dev/null 2>&1; then
  if sudo -n mysqldump -u "$DB_USER_EFFECTIVE" \
    --single-transaction --quick --routines --events "$DB_NAME_EFFECTIVE" >/dev/null 2>&1; then
    sudo -n mysqldump -u "$DB_USER_EFFECTIVE" \
      --single-transaction --quick --routines --events "$DB_NAME_EFFECTIVE"
    exit 0
  fi
fi

if [[ -n "$DB_PASS_FROM_PROP" ]]; then
  MYSQL_PWD="$DB_PASS_FROM_PROP" mysqldump -u "$DB_USER_EFFECTIVE" \
    --single-transaction --quick --routines --events "$DB_NAME_EFFECTIVE"
  exit 0
fi

if [[ -f "$HOME/.my.cnf" ]]; then
  mysqldump --defaults-file="$HOME/.my.cnf" -u "$DB_USER_EFFECTIVE" \
    --single-transaction --quick --routines --events "$DB_NAME_EFFECTIVE"
  exit 0
fi

echo "Errore: dump DB fallito (root socket/sudo non disponibili e nessuna credenziale DB runtime trovata)." >&2
exit 1
REMOTE

echo ">> Dump SQL salvato in: ${DB_DUMP_FILE}"

echo ">> Copio foto/video (e upload correlati) in: ${MEDIA_DIR}"
if [[ "$RSYNC_AVAILABLE" == "true" ]]; then
  echo ">> Uso rsync per la copia media"
  sshpass -p "$PASSWORD" rsync -az \
    -e "ssh -p ${PORT} -o StrictHostKeyChecking=no" \
    "${TARGET_SSH}:${UPLOAD_ROOT_REMOTE%/}/" \
    "${MEDIA_DIR}/"
else
  echo ">> rsync non disponibile: uso fallback scp -r"
  sshpass -p "$PASSWORD" scp -P "$PORT" -r \
    "${TARGET_SSH}:${UPLOAD_ROOT_REMOTE%/}/." \
    "$MEDIA_DIR/"
fi

echo ">> Backup completato con successo."
echo ">> Cartella backup: ${RUN_DIR}"

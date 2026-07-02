#!/usr/bin/env bash
set -euo pipefail

MYSQL_USER_HOST=""
MYSQL_LOGIN_PATH=""
MYSQL_DEFAULTS_FILE=""
MYSQL_ADMIN_USER=""
MYSQL_ADMIN_HOST=""
MYSQL_ADMIN_PORT=""

usage() {
  cat <<'HELP'
Uso:
  ENGINE_GALLERY_DB_URL='jdbc:mysql://db-host:3306/engine_gallery?serverTimezone=UTC' \
  ENGINE_GALLERY_DB_USER='app_user' \
  ENGINE_GALLERY_DB_PASSWORD='APP_PASSWORD_NON_VERSIONATA' \
  ./provision-db-user.sh --mysql-user-host app-host --login-path admin-local

Opzioni:
  --mysql-user-host  Host MySQL da associare all'utente applicativo (obbligatorio)
  --login-path       Login path MySQL amministrativo già configurato (opzionale)
  --defaults-file    File MySQL client con credenziali amministrative (opzionale)
  --admin-user       Utente amministrativo MySQL (opzionale)
  --admin-host       Host MySQL amministrativo (opzionale)
  --admin-port       Porta MySQL amministrativa (opzionale)
  --help, -h         Mostra questo aiuto

Note:
  - Le credenziali applicative vengono lette solo da:
      ENGINE_GALLERY_DB_URL
      ENGINE_GALLERY_DB_USER
      ENGINE_GALLERY_DB_PASSWORD
  - Lo script crea o aggiorna l'utente applicativo senza stampare la password.
  - Lo script non crea lo schema e non elimina utenti legacy automaticamente.
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

require_env() {
  local key="$1"
  local val="${!key:-}"
  if [[ -z "${val// }" ]]; then
    echo "Errore: configurazione obbligatoria mancante: ${key}" >&2
    exit 1
  fi
}

trim() {
  local value="$1"
  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"
  printf '%s' "$value"
}

parse_db_name_from_url() {
  local jdbc_url="$1"
  if [[ "$jdbc_url" != jdbc:mysql://* ]]; then
    echo "Errore: ENGINE_GALLERY_DB_URL deve usare il formato jdbc:mysql://host[:porta]/database[?parametri]" >&2
    exit 1
  fi

  local without_prefix="${jdbc_url#jdbc:mysql://}"
  local db_part="${without_prefix#*/}"
  db_part="${db_part%%\?*}"

  if [[ -z "$db_part" || "$db_part" == "$without_prefix" ]]; then
    echo "Errore: impossibile determinare il nome del database da ENGINE_GALLERY_DB_URL" >&2
    exit 1
  fi

  printf '%s' "$db_part"
}

sql_quote() {
  printf "%s" "${1//\'/\'\'}"
}

identifier_quote() {
  printf "%s" "${1//\`/\`\`}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mysql-user-host)
      require_value "$1" "${2:-}"
      MYSQL_USER_HOST="$2"
      shift 2
      ;;
    --login-path)
      require_value "$1" "${2:-}"
      MYSQL_LOGIN_PATH="$2"
      shift 2
      ;;
    --defaults-file)
      require_value "$1" "${2:-}"
      MYSQL_DEFAULTS_FILE="$2"
      shift 2
      ;;
    --admin-user)
      require_value "$1" "${2:-}"
      MYSQL_ADMIN_USER="$2"
      shift 2
      ;;
    --admin-host)
      require_value "$1" "${2:-}"
      MYSQL_ADMIN_HOST="$2"
      shift 2
      ;;
    --admin-port)
      require_value "$1" "${2:-}"
      MYSQL_ADMIN_PORT="$2"
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

require_env "ENGINE_GALLERY_DB_URL"
require_env "ENGINE_GALLERY_DB_USER"
require_env "ENGINE_GALLERY_DB_PASSWORD"

if [[ -z "${MYSQL_USER_HOST// }" ]]; then
  echo "Errore: --mysql-user-host è obbligatorio per limitare l'utente applicativo all'host corretto." >&2
  exit 1
fi

ENGINE_GALLERY_DB_URL="$(trim "${ENGINE_GALLERY_DB_URL}")"
ENGINE_GALLERY_DB_USER="$(trim "${ENGINE_GALLERY_DB_USER}")"
ENGINE_GALLERY_DB_PASSWORD="$(trim "${ENGINE_GALLERY_DB_PASSWORD}")"
MYSQL_USER_HOST="$(trim "${MYSQL_USER_HOST}")"

if [[ -z "$ENGINE_GALLERY_DB_URL" || -z "$ENGINE_GALLERY_DB_USER" || -z "$ENGINE_GALLERY_DB_PASSWORD" ]]; then
  echo "Errore: URL, utente e password DB non possono essere vuoti o composti solo da spazi." >&2
  exit 1
fi

if [[ "$ENGINE_GALLERY_DB_USER" == "root" ]]; then
  echo "Errore: ENGINE_GALLERY_DB_USER non può essere 'root'; usa un account applicativo dedicato." >&2
  exit 1
fi

DB_NAME="$(parse_db_name_from_url "$ENGINE_GALLERY_DB_URL")"

MYSQL_CMD=(mysql --batch --skip-column-names)

if [[ -n "$MYSQL_LOGIN_PATH" ]]; then
  MYSQL_CMD+=("--login-path=$MYSQL_LOGIN_PATH")
fi
if [[ -n "$MYSQL_DEFAULTS_FILE" ]]; then
  MYSQL_CMD+=("--defaults-file=$MYSQL_DEFAULTS_FILE")
fi
if [[ -n "$MYSQL_ADMIN_USER" ]]; then
  MYSQL_CMD+=("--user=$MYSQL_ADMIN_USER")
fi
if [[ -n "$MYSQL_ADMIN_HOST" ]]; then
  MYSQL_CMD+=("--host=$MYSQL_ADMIN_HOST")
fi
if [[ -n "$MYSQL_ADMIN_PORT" ]]; then
  MYSQL_CMD+=("--port=$MYSQL_ADMIN_PORT")
fi

APP_USER_ESCAPED="$(sql_quote "$ENGINE_GALLERY_DB_USER")"
APP_HOST_ESCAPED="$(sql_quote "$MYSQL_USER_HOST")"
APP_PASSWORD_ESCAPED="$(sql_quote "$ENGINE_GALLERY_DB_PASSWORD")"
DB_NAME_ESCAPED="$(identifier_quote "$DB_NAME")"

echo ">> Provisioning utente MySQL applicativo '${ENGINE_GALLERY_DB_USER}'@'${MYSQL_USER_HOST}' sul database '${DB_NAME}'"
echo ">> Permessi applicati: SELECT, INSERT, UPDATE, DELETE"

"${MYSQL_CMD[@]}" <<SQL
CREATE USER IF NOT EXISTS '${APP_USER_ESCAPED}'@'${APP_HOST_ESCAPED}' IDENTIFIED BY '${APP_PASSWORD_ESCAPED}';
ALTER USER '${APP_USER_ESCAPED}'@'${APP_HOST_ESCAPED}' IDENTIFIED BY '${APP_PASSWORD_ESCAPED}';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM '${APP_USER_ESCAPED}'@'${APP_HOST_ESCAPED}';
GRANT SELECT, INSERT, UPDATE, DELETE ON \`${DB_NAME_ESCAPED}\`.* TO '${APP_USER_ESCAPED}'@'${APP_HOST_ESCAPED}';
FLUSH PRIVILEGES;
SQL

echo ">> Provisioning completato."

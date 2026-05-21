#!/usr/bin/env bash
set -euo pipefail

REMOTE_USER_DEFAULT="root"
REMOTE_HOST_DEFAULT="82.165.20.124"
REMOTE_WEBAPPS_DEFAULT="/opt/tomcat/webapps"
REMOTE_PORT_DEFAULT="22"

usage() {
  cat <<'HELP'
Uso:
  ./deploy-remoto.sh [opzioni]

Esempi:
  ./deploy-remoto.sh
  ./deploy-remoto.sh --skip-build
  ./deploy-remoto.sh --remote-path ~/webapps
  ./deploy-remoto.sh --remote-sql-path ~/sql

Opzioni:
  --skip-build      Salta 'mvn clean package' e usa WAR già presente in target/
  --port            Porta SSH (default: 22)
  --password        Password SSH (se assente viene chiesta in input nascosto)
  --remote-user     Utente SSH remoto (default: root)
  --remote-host     Host SSH remoto (default: 82.165.20.124)
  --remote-path     Cartella remota webapps (default: /opt/tomcat/webapps)
  --remote-sql-path Cartella remota script SQL (default: ~)
  --help, -h        Mostra questo aiuto

Note:
  - Lo script cerca il WAR in target/ e usa quello più recente.
  - Upload WAR e SQL via sshpass + scp.
HELP
}

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

PORT="$REMOTE_PORT_DEFAULT"
PASSWORD=""
REMOTE_USER="$REMOTE_USER_DEFAULT"
REMOTE_HOST="$REMOTE_HOST_DEFAULT"
REMOTE_PATH="$REMOTE_WEBAPPS_DEFAULT"
REMOTE_SQL_PATH=""
SKIP_BUILD="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build)
      SKIP_BUILD="true"
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

if [[ -z "$PASSWORD" ]]; then
  read -r -s -p "Inserisci password SSH per ${REMOTE_USER}@${REMOTE_HOST}: " PASSWORD
  echo
fi

if [[ -z "$PASSWORD" ]]; then
  echo "Errore: password SSH non fornita." >&2
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
TARGET="${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH%/}/"

echo ">> WAR selezionato: $WAR_FILE"

if ! command -v sshpass >/dev/null 2>&1; then
  echo "Errore: 'sshpass' non trovato." >&2
  echo "Installa sshpass per usare il deploy automatico con password." >&2
  exit 1
fi

echo ">> Upload SCP verso: $TARGET"
sshpass -p "$PASSWORD" scp -P "$PORT" "$WAR_FILE" "$TARGET"

SQL_BASE_PATH="${REMOTE_SQL_PATH:-~}"
SQL_BASE_PATH_SHELL="${SQL_BASE_PATH/#\~/\$HOME}"
SQL_ROOT="src/main/resources"
DB_SQL_FILE="${SQL_ROOT}/db.sql"
MIGRATIONS_DIR="${SQL_ROOT}/migrations"
SQL_PATHS=()

if [[ -f "$DB_SQL_FILE" ]]; then
  SQL_PATHS+=("$DB_SQL_FILE")
fi

if [[ -d "$MIGRATIONS_DIR" ]]; then
  while IFS= read -r -d '' migration_file; do
    SQL_PATHS+=("$migration_file")
  done < <(find "$MIGRATIONS_DIR" -type f -name "*.sql" -print0 | sort -z)
else
  echo ">> Cartella migrations non trovata: ${MIGRATIONS_DIR} (skip)."
fi

if [[ "${#SQL_PATHS[@]}" -gt 0 ]]; then
  echo ">> Upload file database in: ${REMOTE_USER}@${REMOTE_HOST}:${SQL_BASE_PATH}"
  for sql_file in "${SQL_PATHS[@]}"; do
    rel_path="${sql_file#${SQL_ROOT}/}"
    remote_dir_shell="${SQL_BASE_PATH_SHELL%/}/$(dirname "$rel_path")"
    remote_target="${SQL_BASE_PATH%/}/${rel_path}"

    sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p ${remote_dir_shell}"
    sshpass -p "$PASSWORD" scp -P "$PORT" "$sql_file" "${REMOTE_USER}@${REMOTE_HOST}:${remote_target}"
  done
else
  echo ">> Nessun file database trovato (${DB_SQL_FILE} o ${MIGRATIONS_DIR}/*.sql)."
fi

echo ">> Deploy completato con successo."

REMOTE_IP="$(resolve_ipv4 "$REMOTE_HOST")"
if [[ "$APP_CONTEXT" == "ROOT" ]]; then
  APP_URL="http://${REMOTE_IP}/"
else
  APP_URL="http://${REMOTE_IP}/${APP_CONTEXT}/"
fi

echo ">> Avvio applicativo (stimato): $APP_URL"

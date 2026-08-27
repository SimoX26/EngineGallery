#!/usr/bin/env bash
set -euo pipefail

REMOTE_USER_DEFAULT="root"
REMOTE_HOST_DEFAULT="82.165.20.124"
REMOTE_WEBAPPS_DEFAULT="/opt/tomcat/webapps"
REMOTE_OPS_PATH_DEFAULT="~/enginegallery-ops"
REMOTE_PORT_DEFAULT="22"

usage() {
  cat <<'HELP'
Uso:
  ./deploy-remoto.sh [opzioni]

Esempi:
  ./deploy-remoto.sh
  ./deploy-remoto.sh --skip-build
  ./deploy-remoto.sh --remote-path ~/webapps
  ./deploy-remoto.sh --remote-ops-path ~/enginegallery-ops

Opzioni:
  --skip-build      Salta 'mvn clean package' e usa WAR già presente in target/
  --port            Porta SSH (default: 22)
  --password        Password SSH (se assente viene chiesta in input nascosto)
  --remote-user     Utente SSH remoto (default: root)
  --remote-host     Host SSH remoto (default: 82.165.20.124)
  --remote-path     Cartella remota webapps (default: /opt/tomcat/webapps)
  --remote-ops-path Cartella remota artefatti operativi (default: ~/enginegallery-ops)
  --remote-sql-path Alias compatibile di --remote-ops-path
  --help, -h        Mostra questo aiuto

Note:
  - Lo script cerca il WAR in target/ e usa quello più recente.
  - Carica il WAR in webapps e gli artefatti operativi in una directory separata.
  - Gli artefatti operativi vengono ricaricati solo se il loro contenuto è cambiato.
  - Non esegue automaticamente provisioning DB, migrazioni SQL o restart servizi.
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

collect_ops_files() {
  OPS_FILES=()

  if [[ -f "provision-db-user.sh" ]]; then
    OPS_FILES+=("admin:provision-db-user.sh")
  fi

  if [[ -f "src/main/resources/db.sql" ]]; then
    OPS_FILES+=("sql:src/main/resources/db.sql")
  fi

  if [[ -d "src/main/resources/migrations" ]]; then
    while IFS= read -r -d '' migration_file; do
      OPS_FILES+=("sql:${migration_file}")
    done < <(find "src/main/resources/migrations" -type f -name "*.sql" -print0 | sort -z)
  fi
}

ops_relative_path() {
  local kind="$1"
  local local_path="$2"

  case "$kind" in
    admin)
      printf 'admin/%s\n' "$(basename "$local_path")"
      ;;
    sql)
      if [[ "$local_path" == src/main/resources/migrations/* ]]; then
        printf 'sql/%s\n' "${local_path#src/main/resources/}"
      else
        printf 'sql/%s\n' "$(basename "$local_path")"
      fi
      ;;
    *)
      echo "Errore: tipo artefatto non supportato: $kind" >&2
      return 1
      ;;
  esac
}

build_ops_manifest() {
  local entry kind local_path relative_path digest

  for entry in "${OPS_FILES[@]}"; do
    kind="${entry%%:*}"
    local_path="${entry#*:}"
    relative_path="$(ops_relative_path "$kind" "$local_path")"
    digest="$(sha256sum "$local_path" | awk '{print $1}')"
    printf '%s  %s\n' "$digest" "$relative_path"
  done | sort
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
REMOTE_OPS_PATH="$REMOTE_OPS_PATH_DEFAULT"
SKIP_BUILD="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build)
      SKIP_BUILD="true"
      shift
      ;;
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
    --remote-path)
      require_value "$1" "${2:-}"
      REMOTE_PATH="$2"
      shift 2
      ;;
    --remote-ops-path)
      require_value "$1" "${2:-}"
      REMOTE_OPS_PATH="$2"
      shift 2
      ;;
    --remote-sql-path)
      require_value "$1" "${2:-}"
      REMOTE_OPS_PATH="$2"
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

collect_ops_files

WAR_FILE="$(ls -t target/*.war 2>/dev/null | head -n 1 || true)"
if [[ -z "$WAR_FILE" ]]; then
  echo "Errore: nessun file WAR trovato in target/." >&2
  exit 1
fi
WAR_NAME="$(basename "$WAR_FILE")"
APP_CONTEXT="${WAR_NAME%.war}"
TARGET="${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH%/}/"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"

echo ">> WAR selezionato: $WAR_FILE"

if ! command -v sshpass >/dev/null 2>&1; then
  echo "Errore: 'sshpass' non trovato." >&2
  echo "Installa sshpass per usare il deploy automatico con password." >&2
  exit 1
fi

OPS_BASE_PATH="${REMOTE_OPS_PATH%/}"
OPS_BASE_PATH_SHELL="${OPS_BASE_PATH/#\~/\$HOME}"
OPS_ADMIN_DIR="${OPS_BASE_PATH}/admin"
OPS_SQL_DIR="${OPS_BASE_PATH}/sql"
OPS_BACKUP_DIR="${OPS_BASE_PATH}/backups"
OPS_ADMIN_DIR_SHELL="${OPS_ADMIN_DIR/#\~/\$HOME}"
OPS_SQL_DIR_SHELL="${OPS_SQL_DIR/#\~/\$HOME}"
OPS_BACKUP_DIR_SHELL="${OPS_BACKUP_DIR/#\~/\$HOME}"
REMOTE_WAR_TARGET="${REMOTE_PATH%/}/${WAR_NAME}"
REMOTE_WAR_TARGET_SHELL="${REMOTE_WAR_TARGET/#\~/\$HOME}"
REMOTE_WAR_TMP="${REMOTE_PATH%/}/.${WAR_NAME}.uploading.${TIMESTAMP}.$$"
REMOTE_WAR_TMP_SHELL="${REMOTE_WAR_TMP/#\~/\$HOME}"
REMOTE_WAR_BACKUP="${OPS_BACKUP_DIR%/}/webapps/${WAR_NAME}.${TIMESTAMP}.bak"
REMOTE_WAR_BACKUP_SHELL="${REMOTE_WAR_BACKUP/#\~/\$HOME}"
REMOTE_OPS_MANIFEST="${OPS_BASE_PATH%/}/.manifest.sha256"
REMOTE_OPS_MANIFEST_SHELL="${REMOTE_OPS_MANIFEST/#\~/\$HOME}"
OPS_MANIFEST_TMP=""
OPS_UPDATED="false"

cleanup_ops_manifest() {
  if [[ -n "$OPS_MANIFEST_TMP" && -f "$OPS_MANIFEST_TMP" ]]; then
    rm -f "$OPS_MANIFEST_TMP"
  fi
}

trap cleanup_ops_manifest EXIT

echo ">> Preparo cartelle remote applicative e operative"
sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p \
  ${REMOTE_PATH/#\~/\$HOME} \
  ${OPS_ADMIN_DIR_SHELL} \
  ${OPS_SQL_DIR_SHELL}/migrations \
  ${OPS_BACKUP_DIR_SHELL}/webapps && chmod 0750 \
  ${OPS_BASE_PATH_SHELL} \
  ${OPS_ADMIN_DIR_SHELL} \
  ${OPS_SQL_DIR_SHELL} \
  ${OPS_SQL_DIR_SHELL}/migrations \
  ${OPS_BACKUP_DIR_SHELL} \
  ${OPS_BACKUP_DIR_SHELL}/webapps"

echo ">> Upload WAR temporaneo verso: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_WAR_TMP}"
sshpass -p "$PASSWORD" scp -P "$PORT" "$WAR_FILE" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_WAR_TMP}"

echo ">> Installo WAR e conservo backup recuperabile della versione precedente"
sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "
  set -euo pipefail
  if [[ -f ${REMOTE_WAR_TARGET_SHELL} ]]; then
    cp -p ${REMOTE_WAR_TARGET_SHELL} ${REMOTE_WAR_BACKUP_SHELL}
  fi
  chmod 0644 ${REMOTE_WAR_TMP_SHELL}
  mv -f ${REMOTE_WAR_TMP_SHELL} ${REMOTE_WAR_TARGET_SHELL}
"

if [[ "${#OPS_FILES[@]}" -gt 0 ]]; then
  if ! command -v sha256sum >/dev/null 2>&1; then
    echo "Errore: 'sha256sum' non trovato, impossibile verificare gli artefatti operativi." >&2
    exit 1
  fi

  LOCAL_OPS_MANIFEST="$(build_ops_manifest)"
  REMOTE_OPS_MANIFEST_CONTENT="$(sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "if [[ -f ${REMOTE_OPS_MANIFEST_SHELL} ]]; then cat ${REMOTE_OPS_MANIFEST_SHELL}; fi")"

  if [[ "$LOCAL_OPS_MANIFEST" == "$REMOTE_OPS_MANIFEST_CONTENT" ]]; then
    echo ">> Artefatti operativi invariati: salto il caricamento di ${OPS_BASE_PATH}"
  else
    OPS_UPDATED="true"
    echo ">> Upload artefatti operativi in: ${REMOTE_USER}@${REMOTE_HOST}:${OPS_BASE_PATH}"
    for entry in "${OPS_FILES[@]}"; do
      kind="${entry%%:*}"
      local_path="${entry#*:}"

      case "$kind" in
        admin)
          remote_dir="${OPS_ADMIN_DIR}"
          remote_dir_shell="${OPS_ADMIN_DIR_SHELL}"
          remote_mode="0750"
          ;;
        sql)
          if [[ "$local_path" == src/main/resources/migrations/* ]]; then
            rel_path="${local_path#src/main/resources/}"
            remote_dir="${OPS_BASE_PATH%/}/sql/$(dirname "$rel_path")"
            remote_dir_shell="${OPS_BASE_PATH_SHELL%/}/sql/$(dirname "$rel_path")"
          else
            remote_dir="${OPS_SQL_DIR}"
            remote_dir_shell="${OPS_SQL_DIR_SHELL}"
          fi
          remote_mode="0640"
          ;;
        *)
          echo "Errore: tipo artefatto non supportato: $kind" >&2
          exit 1
          ;;
      esac

      remote_file="${remote_dir%/}/$(basename "$local_path")"
      remote_file_shell="${remote_file/#\~/\$HOME}"

      sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p ${remote_dir_shell} && chmod 0750 ${remote_dir_shell}"
      sshpass -p "$PASSWORD" scp -P "$PORT" "$local_path" "${REMOTE_USER}@${REMOTE_HOST}:${remote_file}"
      sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "chmod ${remote_mode} ${remote_file_shell}"
    done

    OPS_MANIFEST_TMP="$(mktemp)"
    printf '%s\n' "$LOCAL_OPS_MANIFEST" > "$OPS_MANIFEST_TMP"
    REMOTE_OPS_MANIFEST_TMP="${REMOTE_OPS_MANIFEST}.uploading.$$"
    REMOTE_OPS_MANIFEST_TMP_SHELL="${REMOTE_OPS_MANIFEST_TMP/#\~/\$HOME}"
    sshpass -p "$PASSWORD" scp -P "$PORT" "$OPS_MANIFEST_TMP" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_OPS_MANIFEST_TMP}"
    sshpass -p "$PASSWORD" ssh -p "$PORT" "${REMOTE_USER}@${REMOTE_HOST}" "chmod 0640 ${REMOTE_OPS_MANIFEST_TMP_SHELL} && mv -f ${REMOTE_OPS_MANIFEST_TMP_SHELL} ${REMOTE_OPS_MANIFEST_SHELL}"
  fi
else
  echo ">> Nessun artefatto operativo addizionale trovato."
fi

echo ">> Artefatti caricati:"
echo "   - WAR applicativo: ${REMOTE_WAR_TARGET}"
if [[ "$OPS_UPDATED" == "true" ]]; then
  echo "   - Script amministrativi e SQL: aggiornati in ${OPS_BASE_PATH}"
else
  echo "   - Script amministrativi e SQL: invariati in ${OPS_BASE_PATH}"
fi
echo "   - Backup WAR precedenti: ${OPS_BACKUP_DIR}/webapps"

echo ">> Deploy completato con successo."

REMOTE_IP="$(resolve_ipv4 "$REMOTE_HOST")"
if [[ "$APP_CONTEXT" == "ROOT" ]]; then
  APP_URL="http://${REMOTE_IP}/"
else
  APP_URL="http://${REMOTE_IP}/${APP_CONTEXT}/"
fi

echo ">> Avvio applicativo (stimato): $APP_URL"

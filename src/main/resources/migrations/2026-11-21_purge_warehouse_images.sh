#!/usr/bin/env bash
set -euo pipefail

# Run after 2026-11-21_expand_warehouse.sql, with the webapp stopped.
# Argument: the active upload root's warehouse subdirectory.
if [ "$#" -ne 1 ]; then
    printf 'Uso: %s /percorso/upload/warehouse\n' "$0" >&2
    exit 2
fi

warehouse_dir=$(realpath -- "$1")
if [ ! -d "$warehouse_dir" ] || [ "$(basename -- "$warehouse_dir")" != warehouse ]; then
    printf 'Percorso non valido: serve la directory warehouse degli upload.\n' >&2
    exit 2
fi

for item_dir in "$warehouse_dir"/*; do
    [ -d "$item_dir" ] || continue
    item_id=$(basename -- "$item_dir")
    [[ "$item_id" =~ ^[0-9]+$ ]] || continue
    rm -rf -- "$item_dir"
done

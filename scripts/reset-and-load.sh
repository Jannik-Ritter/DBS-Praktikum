#!/usr/bin/env bash
# Reset DB stack and run the data loader.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif docker-compose version >/dev/null 2>&1; then
    docker-compose "$@"
  else
    echo "Error: neither 'docker compose' nor 'docker-compose' is available." >&2
    exit 1
  fi
}

run() {
  echo "+ $*"
  compose "$@"
}

run down
run up -d db adminer
run --profile tools run --rm loader \
  mvn -q clean compile exec:java \
  -Dexec.args="--schema src/dbs-schema.sql --data-dir data --rejects build/rejected-records.csv"

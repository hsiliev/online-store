#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

(
  cd "$SCRIPT_DIR"
  docker volume prune -f
  docker image prune -f
  docker builder prune -f
  docker compose up --detach
)

sleep 3
open http://localhost:8083

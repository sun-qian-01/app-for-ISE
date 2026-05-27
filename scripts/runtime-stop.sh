#!/usr/bin/env bash
set -Eeuo pipefail

SERVICE="app-for-ise.service"

if [[ "${EUID}" -eq 0 ]]; then
  systemctl stop "${SERVICE}"
  systemctl stop nginx
else
  sudo systemctl stop "${SERVICE}"
  sudo systemctl stop nginx
fi

echo "runtime stopped"

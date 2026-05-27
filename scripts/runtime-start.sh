#!/usr/bin/env bash
set -Eeuo pipefail

SERVICE="app-for-ise.service"

if [[ "${EUID}" -eq 0 ]]; then
  systemctl restart "${SERVICE}"
  systemctl restart nginx
else
  sudo systemctl restart "${SERVICE}"
  sudo systemctl restart nginx
fi

curl -sS "http://127.0.0.1/" >/dev/null
curl -sS "http://127.0.0.1/api/v1/auth/me" >/dev/null
echo "runtime started"

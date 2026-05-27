#!/usr/bin/env bash
set -Eeuo pipefail

SERVICE="app-for-ise.service"

if [[ "${EUID}" -eq 0 ]]; then
  systemctl status "${SERVICE}" --no-pager || true
  systemctl status nginx --no-pager || true
else
  sudo systemctl status "${SERVICE}" --no-pager || true
  sudo systemctl status nginx --no-pager || true
fi

echo "--- local health ---"
curl -sS -o /dev/null -w "http://127.0.0.1 -> %{http_code}\n" "http://127.0.0.1/" || true
curl -sS -o /dev/null -w "http://127.0.0.1/api/v1/auth/me -> %{http_code}\n" "http://127.0.0.1/api/v1/auth/me" || true

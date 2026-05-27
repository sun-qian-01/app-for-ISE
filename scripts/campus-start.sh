#!/usr/bin/env bash
#
# campus-start.sh
# ---------------
# 目的：
#   在已经完成部署（执行过 campus-one-click.sh）后，快速一键拉起服务。
#
# 做的事：
#   1) 启动/重启 app-for-ise 后端 systemd 服务
#   2) 启动/重启 nginx
#   3) 做本机健康检查（首页 + /api/v1/auth/me）
#
# 用法：
#   ./scripts/campus-start.sh

set -Eeuo pipefail

APP_NAME="app-for-ise"
SERVICE_NAME="${APP_NAME}.service"

run_root() {
  local cmd="$1"
  if [[ "${EUID}" -eq 0 ]]; then
    bash -lc "${cmd}"
  else
    sudo bash -lc "${cmd}"
  fi
}

echo "[INFO] 启动后端服务..."
run_root "systemctl enable --now '${SERVICE_NAME}'"
run_root "systemctl restart '${SERVICE_NAME}'"

echo "[INFO] 启动 Nginx..."
run_root "systemctl enable --now nginx"
run_root "systemctl restart nginx"

echo "[INFO] 健康检查..."
curl -sS "http://127.0.0.1/" >/dev/null
curl -sS "http://127.0.0.1/api/v1/auth/me" >/dev/null

echo "[INFO] 服务已启动。"

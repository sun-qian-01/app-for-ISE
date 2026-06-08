#!/usr/bin/env bash
#
# runtime-install.sh
# ------------------
# 用途：
#   在“仅部署服务器”上安装并启动 app-for-ise 运行环境。
#
# 适用场景：
#   - 服务器不参与开发，只负责运行。
#   - 你已经从开发机拿到了发布包（包含 backend jar + frontend dist）。
#
# 发布包结构要求（默认）：
#   <bundle-root>/
#     backend/app-for-ise-backend.jar
#     frontend/index.html
#     config/app.env              (可选，RAG/AI 运行配置)
#     scripts/runtime-install.sh   (本脚本)
#
# 功能：
#   1) 安装运行时依赖（java17 + nginx + rsync + curl）
#   2) 部署后端 jar 到 /opt/app-for-ise/backend/
#   3) 部署前端静态资源到 /var/www/app-for-ise/
#   4) 写入 systemd 服务与 Nginx 配置
#   5) 启动并健康检查
#
# 示例：
#   ./scripts/runtime-install.sh --public-ip 10.77.110.167
#
# 说明：
#   - 会修改系统目录（/etc、/opt、/var/www），需要 root/sudo。
#   - 重复执行是幂等的，可用于覆盖升级。

set -Eeuo pipefail

SERVICE_NAME="app-for-ise"
SERVICE_UNIT="${SERVICE_NAME}.service"
SERVICE_USER="appforise"

INSTALL_DIR="/opt/${SERVICE_NAME}"
WEB_ROOT="/var/www/${SERVICE_NAME}"
BACKEND_PORT="8080"
BACKEND_BIND="127.0.0.1"

PUBLIC_IP=""
EXTRA_SERVER_NAME=""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUNDLE_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SOURCE_JAR="${BUNDLE_ROOT}/backend/app-for-ise-backend.jar"
SOURCE_WEB="${BUNDLE_ROOT}/frontend"
SOURCE_ENV="${BUNDLE_ROOT}/config/app.env"
TARGET_ENV="${INSTALL_DIR}/config/app.env"

SYSTEMD_PATH="/etc/systemd/system/${SERVICE_UNIT}"

log() {
  printf '[INFO] %s\n' "$*"
}

err() {
  printf '[ERROR] %s\n' "$*" >&2
}

usage() {
  cat <<'EOF'
用法:
  ./scripts/runtime-install.sh [选项]

选项:
  --public-ip <IP>         校园网访问 IP（建议显式传）
  --server-name <NAME>     追加 server_name（域名或别名）
  --install-dir <DIR>      后端安装目录，默认 /opt/app-for-ise
  --web-root <DIR>         前端静态目录，默认 /var/www/app-for-ise
  --service-user <USER>    systemd 运行用户，默认 appforise
  -h, --help               显示帮助
EOF
}

run_root() {
  local cmd="$1"
  if [[ "${EUID}" -eq 0 ]]; then
    bash -lc "${cmd}"
  else
    sudo bash -lc "${cmd}"
  fi
}

need_cmd() {
  local cmd="$1"
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    err "缺少命令: ${cmd}"
    exit 1
  fi
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --public-ip)
        PUBLIC_IP="${2:-}"
        shift 2
        ;;
      --server-name)
        EXTRA_SERVER_NAME="${2:-}"
        shift 2
        ;;
      --install-dir)
        INSTALL_DIR="${2:-}"
        shift 2
        ;;
      --web-root)
        WEB_ROOT="${2:-}"
        shift 2
        ;;
      --service-user)
        SERVICE_USER="${2:-}"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        err "未知参数: $1"
        usage
        exit 1
        ;;
    esac
  done
}

detect_public_ip() {
  if [[ -n "${PUBLIC_IP}" ]]; then
    return
  fi
  PUBLIC_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
  if [[ -z "${PUBLIC_IP}" ]]; then
    err "无法自动识别 IP，请显式传入 --public-ip。"
    exit 1
  fi
}

ensure_sources() {
  if [[ ! -f "${SOURCE_JAR}" ]]; then
    err "缺少后端 jar: ${SOURCE_JAR}"
    exit 1
  fi
  if [[ ! -f "${SOURCE_WEB}/index.html" ]]; then
    err "缺少前端静态文件: ${SOURCE_WEB}/index.html"
    exit 1
  fi
}

install_runtime_deps() {
  log "检查并安装运行时依赖..."
  if command -v apt-get >/dev/null 2>&1; then
    run_root "apt-get update"
    run_root "apt-get install -y openjdk-17-jre-headless nginx rsync curl"
  elif command -v dnf >/dev/null 2>&1; then
    run_root "dnf install -y java-17-openjdk nginx rsync curl"
  elif command -v yum >/dev/null 2>&1; then
    run_root "yum install -y java-17-openjdk nginx rsync curl"
  else
    err "未识别包管理器（apt/dnf/yum），请手工安装 java17、nginx、rsync、curl 后重试。"
    exit 1
  fi
}

prepare_user_and_dirs() {
  log "准备服务用户与目录..."
  run_root "if ! id -u '${SERVICE_USER}' >/dev/null 2>&1; then useradd --system --no-create-home --shell /usr/sbin/nologin '${SERVICE_USER}' || useradd -r -s /sbin/nologin '${SERVICE_USER}'; fi"
  run_root "mkdir -p '${INSTALL_DIR}/backend' '${INSTALL_DIR}/config' '${INSTALL_DIR}/data' '${WEB_ROOT}'"
  run_root "chown -R '${SERVICE_USER}:${SERVICE_USER}' '${INSTALL_DIR}'"
}

deploy_backend() {
  log "部署后端 jar..."
  run_root "install -m 0644 '${SOURCE_JAR}' '${INSTALL_DIR}/backend/app-for-ise-backend.jar'"
  run_root "chown '${SERVICE_USER}:${SERVICE_USER}' '${INSTALL_DIR}/backend/app-for-ise-backend.jar'"
}

deploy_frontend() {
  log "部署前端静态资源..."
  run_root "rsync -a --delete '${SOURCE_WEB}/' '${WEB_ROOT}/'"
}

deploy_env() {
  log "部署运行环境配置..."
  if [[ -f "${SOURCE_ENV}" ]]; then
    run_root "install -m 0600 '${SOURCE_ENV}' '${TARGET_ENV}'"
  else
    local tmp
    tmp="$(mktemp)"
    cat > "${tmp}" <<'EOF'
RAG_ENABLED=true
RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1
RAG_LLM_API_KEY=
RAG_LLM_MODEL=gpt-5.4
CURL_NO_PROXY=*
EOF
    run_root "install -m 0600 '${tmp}' '${TARGET_ENV}'"
    rm -f "${tmp}"
  fi
  run_root "chown '${SERVICE_USER}:${SERVICE_USER}' '${TARGET_ENV}'"
}

write_systemd_unit() {
  log "写入 systemd 服务: ${SYSTEMD_PATH}"
  local tmp
  tmp="$(mktemp)"
  cat > "${tmp}" <<EOF
[Unit]
Description=app-for-ise backend service
After=network.target

[Service]
Type=simple
User=${SERVICE_USER}
WorkingDirectory=${INSTALL_DIR}
EnvironmentFile=-${TARGET_ENV}
ExecStart=/usr/bin/java -jar ${INSTALL_DIR}/backend/app-for-ise-backend.jar --server.port=${BACKEND_PORT} --server.address=${BACKEND_BIND}
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF
  run_root "install -m 0644 '${tmp}' '${SYSTEMD_PATH}'"
  rm -f "${tmp}"
  run_root "systemctl daemon-reload"
  run_root "systemctl enable --now '${SERVICE_UNIT}'"
}

write_nginx_config() {
  log "写入 Nginx 配置..."
  local server_name="${PUBLIC_IP} _"
  if [[ -n "${EXTRA_SERVER_NAME}" ]]; then
    server_name="${PUBLIC_IP} ${EXTRA_SERVER_NAME} _"
  fi

  local nginx_target=""
  local symlink_cmd=""

  if [[ -d /etc/nginx/sites-available && -d /etc/nginx/sites-enabled ]]; then
    nginx_target="/etc/nginx/sites-available/${SERVICE_NAME}"
    symlink_cmd="ln -sf '${nginx_target}' '/etc/nginx/sites-enabled/${SERVICE_NAME}'"
  else
    nginx_target="/etc/nginx/conf.d/${SERVICE_NAME}.conf"
  fi

  local tmp
  tmp="$(mktemp)"
  cat > "${tmp}" <<EOF
server {
    listen 80;
    server_name ${server_name};

    root ${WEB_ROOT};
    index index.html;

    location /api/ {
        proxy_pass http://${BACKEND_BIND}:${BACKEND_PORT};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF

  run_root "install -m 0644 '${tmp}' '${nginx_target}'"
  rm -f "${tmp}"
  if [[ -n "${symlink_cmd}" ]]; then
    run_root "${symlink_cmd}"
    run_root "if [ -L /etc/nginx/sites-enabled/default ]; then rm -f /etc/nginx/sites-enabled/default; fi"
  fi

  run_root "nginx -t"
  run_root "systemctl enable --now nginx"
  run_root "systemctl restart nginx"
}

open_firewall() {
  log "尝试放行 80/tcp..."
  if command -v ufw >/dev/null 2>&1; then
    run_root "ufw allow 80/tcp || true"
  elif command -v firewall-cmd >/dev/null 2>&1; then
    run_root "firewall-cmd --permanent --add-service=http || true"
    run_root "firewall-cmd --reload || true"
  fi
}

health_check() {
  log "健康检查..."
  run_root "systemctl restart '${SERVICE_UNIT}'"
  curl -sS "http://127.0.0.1/" >/dev/null
  curl -sS "http://127.0.0.1/api/v1/auth/me" >/dev/null
}

summary() {
  cat <<EOF

部署完成。

访问地址：
  http://${PUBLIC_IP}

服务管理：
  sudo systemctl status ${SERVICE_UNIT}
  sudo systemctl restart ${SERVICE_UNIT}
  sudo systemctl status nginx
  sudo systemctl restart nginx
EOF
}

main() {
  parse_args "$@"
  detect_public_ip
  ensure_sources
  need_cmd curl
  install_runtime_deps
  prepare_user_and_dirs
  deploy_backend
  deploy_frontend
  deploy_env
  write_systemd_unit
  write_nginx_config
  open_firewall
  health_check
  summary
}

main "$@"

#!/usr/bin/env bash
#
# campus-one-click.sh
# -------------------
# 目的：
#   将本仓库一键部署为“校园网可访问”的运行形态，默认通过 Nginx 暴露 80 端口，
#   对外地址为：http://<校园网IP>，并由 Nginx 反向代理后端 API。
#
# 你将得到：
#   1) systemd 常驻后端服务（Spring Boot, 127.0.0.1:8080）
#   2) Nginx 托管前端静态文件（/var/www/app-for-ise）
#   3) /api/* 反向代理到后端
#   4) 一条命令可重复执行（幂等更新）
#
# 典型用法：
#   ./scripts/campus-one-click.sh --public-ip 10.77.110.167
#
# 说明：
#   - 本脚本“会修改系统配置”（/etc/systemd, /etc/nginx, /var/www），因此需要 root 权限。
#   - 若你不是 root，脚本会尝试 sudo；若 sudo 不可用，会直接给出明确提示并退出。
#   - 脚本默认会构建前后端；若你确认构建产物已存在，可加 --skip-build。
#
# 安全默认值：
#   - 后端仅绑定 127.0.0.1:8080，避免直接暴露给外网。
#   - 对外流量统一走 Nginx:80。

set -Eeuo pipefail

APP_NAME="app-for-ise"
BACKEND_PORT="8080"
BACKEND_BIND="127.0.0.1"
FRONTEND_PORT="80"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
FRONTEND_DIR="${REPO_ROOT}/web"
DEPLOY_DIR="${REPO_ROOT}/.deploy"
DEPLOY_JAR_PATH="${DEPLOY_DIR}/${APP_NAME}-backend.jar"

SERVICE_NAME="${APP_NAME}.service"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}"

NGINX_SITE="/etc/nginx/sites-available/${APP_NAME}"
NGINX_ENABLED="/etc/nginx/sites-enabled/${APP_NAME}"
WEB_ROOT="/var/www/${APP_NAME}"

SKIP_BUILD="0"
PUBLIC_IP=""
SERVER_NAME=""

log() {
  printf '[INFO] %s\n' "$*"
}

warn() {
  printf '[WARN] %s\n' "$*" >&2
}

err() {
  printf '[ERROR] %s\n' "$*" >&2
}

usage() {
  cat <<'EOF'
用法:
  ./scripts/campus-one-click.sh [选项]

选项:
  --public-ip <IP>      指定校园网访问 IP（例如 10.77.110.167）
  --server-name <NAME>  额外指定 Nginx server_name（可填域名或IP）
  --skip-build          跳过前后端构建步骤
  -h, --help            显示帮助
EOF
}

need_cmd() {
  local cmd="$1"
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    err "缺少命令: ${cmd}"
    return 1
  fi
}

# 统一执行“需要 root”的命令。
# 优先 root 直接执行；否则尝试 sudo。
run_root() {
  local cmd="$1"
  if [[ "${EUID}" -eq 0 ]]; then
    bash -lc "${cmd}"
    return $?
  fi

  if ! command -v sudo >/dev/null 2>&1; then
    err "当前不是 root 且系统无 sudo。请切换 root 后重试。"
    return 1
  fi

  # 这里不使用 sudo -n，避免无密码 sudo 之外的场景直接失败。
  sudo bash -lc "${cmd}"
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --public-ip)
        PUBLIC_IP="${2:-}"
        shift 2
        ;;
      --server-name)
        SERVER_NAME="${2:-}"
        shift 2
        ;;
      --skip-build)
        SKIP_BUILD="1"
        shift
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

detect_ip() {
  if [[ -n "${PUBLIC_IP}" ]]; then
    return 0
  fi

  # 从主机网卡中拿第一个 IPv4 地址作为默认访问地址。
  # 注意：如果机器有多网卡，建议显式传入 --public-ip。
  PUBLIC_IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
  if [[ -z "${PUBLIC_IP}" ]]; then
    err "无法自动识别本机 IP，请手动指定 --public-ip。"
    exit 1
  fi
}

resolve_server_name() {
  # server_name 至少放一个可访问地址，若用户指定了域名/别名则一并追加。
  if [[ -n "${SERVER_NAME}" ]]; then
    SERVER_NAME="${PUBLIC_IP} ${SERVER_NAME} _"
  else
    SERVER_NAME="${PUBLIC_IP} _"
  fi
}

ensure_prerequisites() {
  need_cmd java
  need_cmd mvn
  need_cmd node
  need_cmd npm
  need_cmd curl

  # nginx 和 systemctl 在 root 侧可能需要安装；先判断存在性并在后面安装。
  if ! command -v systemctl >/dev/null 2>&1; then
    err "系统缺少 systemctl，无法创建常驻服务。"
    exit 1
  fi
}

build_backend() {
  log "构建后端（mvn -DskipTests package）..."
  (cd "${REPO_ROOT}" && mvn -DskipTests package)

  # 选择最新的可执行 jar，排除 original-*.jar。
  local jar_path
  jar_path="$(cd "${REPO_ROOT}" && ls -1t target/*.jar 2>/dev/null | grep -v 'original-' | head -n 1 || true)"
  if [[ -z "${jar_path}" ]]; then
    err "后端构建完成但未找到 target/*.jar。"
    exit 1
  fi

  mkdir -p "${DEPLOY_DIR}"
  cp -f "${REPO_ROOT}/${jar_path}" "${DEPLOY_JAR_PATH}"
  log "后端 jar 已就位: ${DEPLOY_JAR_PATH}"
}

build_frontend() {
  log "构建前端（npm run build）..."
  # 这里优先 npm ci，若环境不存在 lock 的完整上下文，再回落 npm install。
  if [[ -f "${FRONTEND_DIR}/package-lock.json" ]]; then
    (cd "${FRONTEND_DIR}" && npm ci)
  else
    (cd "${FRONTEND_DIR}" && npm install)
  fi
  (cd "${FRONTEND_DIR}" && npm run build)

  if [[ ! -f "${FRONTEND_DIR}/dist/index.html" ]]; then
    err "前端构建后未找到 dist/index.html。"
    exit 1
  fi
}

install_backend_service() {
  local app_user
  app_user="$(stat -c '%U' "${REPO_ROOT}")"

  log "写入 systemd 服务: ${SERVICE_FILE}"
  local service_tmp
  service_tmp="$(mktemp)"
  cat > "${service_tmp}" <<EOF
[Unit]
Description=${APP_NAME} backend service
After=network.target

[Service]
Type=simple
User=${app_user}
WorkingDirectory=${REPO_ROOT}
ExecStart=/usr/bin/java -jar ${DEPLOY_JAR_PATH} --server.port=${BACKEND_PORT} --server.address=${BACKEND_BIND}
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF

  run_root "install -m 0644 '${service_tmp}' '${SERVICE_FILE}'"
  rm -f "${service_tmp}"

  run_root "systemctl daemon-reload"
  run_root "systemctl enable --now '${SERVICE_NAME}'"
  run_root "systemctl restart '${SERVICE_NAME}'"
}

ensure_nginx_installed() {
  if command -v nginx >/dev/null 2>&1; then
    return 0
  fi

  log "检测到 nginx 未安装，尝试自动安装..."
  if command -v apt-get >/dev/null 2>&1; then
    run_root "apt-get update && apt-get install -y nginx"
  elif command -v dnf >/dev/null 2>&1; then
    run_root "dnf install -y nginx"
  elif command -v yum >/dev/null 2>&1; then
    run_root "yum install -y nginx"
  else
    err "无法识别包管理器，无法自动安装 nginx。请手工安装后重试。"
    exit 1
  fi
}

install_frontend_static() {
  log "发布前端静态文件到 ${WEB_ROOT}"
  run_root "mkdir -p '${WEB_ROOT}'"
  run_root "rsync -a --delete '${FRONTEND_DIR}/dist/' '${WEB_ROOT}/'"
}

install_nginx_site() {
  log "写入 Nginx 站点配置: ${NGINX_SITE}"
  local nginx_tmp
  nginx_tmp="$(mktemp)"
  cat > "${nginx_tmp}" <<EOF
server {
    listen ${FRONTEND_PORT};
    server_name ${SERVER_NAME};

    root ${WEB_ROOT};
    index index.html;

    # 所有 /api/* 请求反向代理到本机后端。
    # 注意这里不加 URI 尾缀，保证 /api/v1/... 原样透传给 Spring Boot。
    location /api/ {
        proxy_pass http://${BACKEND_BIND}:${BACKEND_PORT};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # Vue Router history 模式关键配置：
    # 当路径不是静态文件时，统一回退到 index.html，由前端路由接管。
    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF

  run_root "install -m 0644 '${nginx_tmp}' '${NGINX_SITE}'"
  rm -f "${nginx_tmp}"

  run_root "ln -sf '${NGINX_SITE}' '${NGINX_ENABLED}'"

  # 可选：禁用默认站点，避免默认页抢占 80 端口路由。
  run_root "if [ -L /etc/nginx/sites-enabled/default ]; then rm -f /etc/nginx/sites-enabled/default; fi"

  run_root "nginx -t"
  run_root "systemctl enable --now nginx"
  run_root "systemctl restart nginx"
}

open_firewall_if_possible() {
  log "尝试放行 80/tcp（若系统启用了防火墙）..."
  if command -v ufw >/dev/null 2>&1; then
    run_root "ufw allow 80/tcp || true"
  elif command -v firewall-cmd >/dev/null 2>&1; then
    run_root "firewall-cmd --permanent --add-service=http || true"
    run_root "firewall-cmd --reload || true"
  else
    warn "未检测到 ufw/firewalld，跳过防火墙自动放行。"
  fi
}

health_check() {
  log "执行健康检查..."

  # 1) 检查后端服务状态
  run_root "systemctl is-active '${SERVICE_NAME}' >/dev/null"

  # 2) 检查 Nginx 站点可访问
  curl -sS "http://127.0.0.1/" >/dev/null

  # 3) 检查 API 反向代理链路（预期返回 JSON，HTTP 200）
  curl -sS "http://127.0.0.1/api/v1/auth/me" >/dev/null

  log "健康检查通过。"
}

print_summary() {
  cat <<EOF

部署完成。

访问地址（校园网内）:
  http://${PUBLIC_IP}

常用运维命令:
  sudo systemctl status ${SERVICE_NAME}
  sudo systemctl restart ${SERVICE_NAME}
  sudo systemctl status nginx
  sudo systemctl restart nginx

更新发布（同一脚本重复执行即可）:
  ./scripts/campus-one-click.sh --public-ip ${PUBLIC_IP}
EOF
}

main() {
  parse_args "$@"
  detect_ip
  resolve_server_name
  ensure_prerequisites

  log "部署目标 IP: ${PUBLIC_IP}"
  log "仓库根目录: ${REPO_ROOT}"

  if [[ "${SKIP_BUILD}" != "1" ]]; then
    build_backend
    build_frontend
  else
    log "跳过构建步骤（--skip-build）"
  fi

  ensure_nginx_installed
  install_backend_service
  install_frontend_static
  install_nginx_site
  open_firewall_if_possible
  health_check
  print_summary
}

main "$@"

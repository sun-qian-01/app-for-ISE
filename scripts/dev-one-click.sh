#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# dev-one-click.sh
# -----------------------------------------------------------------------------
# 一键启动本地开发环境（后端 + 前端），解决以下问题：
# 1) mvn/spring-boot:run 会占用前台，导致 npm run dev 无法继续执行
# 2) 希望一次命令即可后台拉起、查看状态、停止服务
#
# 默认行为：
# - 后端：Spring Boot（端口 8080）
# - 前端：Vite dev server（端口 5173，host=0.0.0.0）
# - 两者均后台运行，日志写入 .run/ 目录
#
# 用法：
#   ./scripts/dev-one-click.sh                # 等价于 start
#   ./scripts/dev-one-click.sh start
#   ./scripts/dev-one-click.sh stop
#   ./scripts/dev-one-click.sh restart
#   ./scripts/dev-one-click.sh status
#   ./scripts/dev-one-click.sh logs
#
# 说明：
# - 脚本会优先加载 scripts/.env.rag.local（如果存在）。
# - 若未设置 RAG_LLM_API_KEY，会自动使用当前项目默认 demo key（你已确认可用）。
# - 脚本不需要 sudo，不修改系统配置。
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
WEB_DIR="${REPO_ROOT}/web"
RUN_DIR="${REPO_ROOT}/.run"

BACKEND_PID_FILE="${RUN_DIR}/backend.pid"
FRONTEND_PID_FILE="${RUN_DIR}/frontend.pid"
BACKEND_LOG="${RUN_DIR}/backend.log"
FRONTEND_LOG="${RUN_DIR}/frontend.log"

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
FRONTEND_HOST="${FRONTEND_HOST:-0.0.0.0}"
FRONTEND_POLLING="${FRONTEND_POLLING:-true}"

DEMO_KEY="sk-c324e1b52a086c8b595f7ba1f290683b4da37a2b75faaf779d4a674f9012c2c3"

log() {
  printf '[INFO] %s\n' "$*"
}

warn() {
  printf '[WARN] %s\n' "$*" >&2
}

err() {
  printf '[ERROR] %s\n' "$*" >&2
}

need_cmd() {
  local cmd="$1"
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    err "缺少命令: ${cmd}"
    exit 1
  fi
}

is_pid_running() {
  local pid="$1"
  [[ -n "${pid}" ]] && kill -0 "${pid}" >/dev/null 2>&1
}

backend_alive() {
  curl --noproxy '*' -sS -m 2 \
    -X POST "http://127.0.0.1:${BACKEND_PORT}/api/v1/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"20220001","password":"123456"}' \
    | rg -q '"code":0'
}

frontend_alive() {
  curl --noproxy '*' -sS -m 2 "http://127.0.0.1:${FRONTEND_PORT}" >/dev/null 2>&1
}

read_pid() {
  local pid_file="$1"
  if [[ -f "${pid_file}" ]]; then
    tr -d '[:space:]' < "${pid_file}"
  fi
}

collect_descendants() {
  local parent="$1"
  local child
  for child in $(pgrep -P "${parent}" 2>/dev/null || true); do
    printf '%s\n' "${child}"
    collect_descendants "${child}"
  done
}

any_pid_in_list_running() {
  local pid
  while read -r pid; do
    [[ -n "${pid}" ]] && is_pid_running "${pid}" && return 0
  done
  return 1
}

remove_pid_file_if_stale() {
  local pid_file="$1"
  local pid
  pid="$(read_pid "${pid_file}")"
  if [[ -n "${pid}" ]] && ! is_pid_running "${pid}"; then
    rm -f "${pid_file}"
  fi
}

load_env() {
  local env_file="${SCRIPT_DIR}/.env.rag.local"
  if [[ -f "${env_file}" ]]; then
    # 自动加载你本地的 RAG 配置，避免每次手动 source
    set -a
    # shellcheck disable=SC1090
    source "${env_file}"
    set +a
    log "已加载环境文件: ${env_file}"
  fi

  export RAG_ENABLED="${RAG_ENABLED:-true}"
  export CODEX_QA_ENABLED="${CODEX_QA_ENABLED:-true}"
  export CODEX_QA_COMMAND="${CODEX_QA_COMMAND:-codex}"
  export CODEX_QA_MODEL="${CODEX_QA_MODEL:-gpt-5.4-mini}"
  export CODEX_QA_WORKDIR="${CODEX_QA_WORKDIR:-${REPO_ROOT}}"
  export CODEX_QA_TIMEOUT_MS="${CODEX_QA_TIMEOUT_MS:-180000}"
  export RAG_LLM_BASE_URL="${RAG_LLM_BASE_URL:-https://gmn.chuangzuoli.com/v1}"
  export RAG_LLM_MODEL="${RAG_LLM_MODEL:-gpt-5.4}"
  export RAG_LLM_API_KEY="${RAG_LLM_API_KEY:-${DEMO_KEY}}"

  export RAG_EMBED_BASE_URL="${RAG_EMBED_BASE_URL:-${RAG_LLM_BASE_URL}}"
  export RAG_EMBED_API_KEY="${RAG_EMBED_API_KEY:-${RAG_LLM_API_KEY}}"
  export RAG_EMBED_MODEL="${RAG_EMBED_MODEL:-text-embedding-3-small}"

  export RAG_VECTOR_ENDPOINT="${RAG_VECTOR_ENDPOINT:-http://127.0.0.1:6333}"
  export RAG_VECTOR_COLLECTION="${RAG_VECTOR_COLLECTION:-kb_article_chunks_v1}"
  export RAG_VECTOR_DIMENSION="${RAG_VECTOR_DIMENSION:-1536}"
}

choose_backend_command() {
  if [[ -x "${REPO_ROOT}/mvnw" ]]; then
    printf '%s' "./mvnw spring-boot:run"
    return 0
  fi
  if command -v mvn >/dev/null 2>&1; then
    printf '%s' "mvn spring-boot:run"
    return 0
  fi
  err "未找到 Maven（mvn/mvnw）。"
  exit 1
}

start_backend() {
  remove_pid_file_if_stale "${BACKEND_PID_FILE}"
  local pid
  pid="$(read_pid "${BACKEND_PID_FILE}")"
  if [[ -n "${pid}" ]] && is_pid_running "${pid}"; then
    log "后端已在运行（PID=${pid}）。"
    return 0
  fi
  if backend_alive; then
    log "检测到后端已运行（非本脚本托管进程），跳过重复启动。"
    return 0
  fi

  local backend_cmd
  backend_cmd="$(choose_backend_command)"
  mkdir -p "${RUN_DIR}"

  log "启动后端（后台）..."
  (
    cd "${REPO_ROOT}"
    nohup bash -lc "${backend_cmd}" > "${BACKEND_LOG}" 2>&1 &
    echo $! > "${BACKEND_PID_FILE}"
  )
  log "后端已启动，日志: ${BACKEND_LOG}"
}

wait_backend_ready() {
  log "等待后端就绪（127.0.0.1:${BACKEND_PORT}）..."
  local i
  for i in $(seq 1 90); do
    if curl --noproxy '*' -sS -m 2 \
      -X POST "http://127.0.0.1:${BACKEND_PORT}/api/v1/auth/login" \
      -H 'Content-Type: application/json' \
      -d '{"username":"20220001","password":"123456"}' \
      | rg -q '"code":0'; then
      log "后端就绪。"
      return 0
    fi
    sleep 1
  done

  err "后端在 90 秒内未就绪。可查看日志: ${BACKEND_LOG}"
  tail -n 40 "${BACKEND_LOG}" || true
  exit 1
}

start_frontend() {
  remove_pid_file_if_stale "${FRONTEND_PID_FILE}"
  local pid
  pid="$(read_pid "${FRONTEND_PID_FILE}")"
  if [[ -n "${pid}" ]] && is_pid_running "${pid}"; then
    log "前端已在运行（PID=${pid}）。"
    return 0
  fi
  if frontend_alive; then
    log "检测到前端已运行（非本脚本托管进程），跳过重复启动。"
    return 0
  fi

  mkdir -p "${RUN_DIR}"
  if [[ ! -d "${WEB_DIR}/node_modules" ]]; then
    log "检测到前端依赖未安装，先执行 npm install..."
    (cd "${WEB_DIR}" && npm install)
  fi

  log "启动前端（后台，host=${FRONTEND_HOST}, port=${FRONTEND_PORT}, polling=${FRONTEND_POLLING}）..."
  (
    cd "${WEB_DIR}"
    nohup env CHOKIDAR_USEPOLLING="${FRONTEND_POLLING}" npm run dev -- --host "${FRONTEND_HOST}" --port "${FRONTEND_PORT}" > "${FRONTEND_LOG}" 2>&1 &
    echo $! > "${FRONTEND_PID_FILE}"
  )
  log "前端已启动，日志: ${FRONTEND_LOG}"
}

wait_frontend_ready() {
  log "等待前端就绪（127.0.0.1:${FRONTEND_PORT}）..."
  local i
  for i in $(seq 1 45); do
    if curl --noproxy '*' -sS -m 2 "http://127.0.0.1:${FRONTEND_PORT}" >/dev/null 2>&1; then
      log "前端就绪。"
      return 0
    fi
    sleep 1
  done
  warn "前端可能尚未就绪，请查看日志: ${FRONTEND_LOG}"
}

stop_by_pid_file() {
  local name="$1"
  local pid_file="$2"
  local pid
  pid="$(read_pid "${pid_file}")"

  if [[ -z "${pid}" ]]; then
    log "${name} 未运行（无 PID 文件）。"
    return 0
  fi

  if ! is_pid_running "${pid}"; then
    log "${name} 已停止（清理旧 PID 文件）。"
    rm -f "${pid_file}"
    return 0
  fi

  local descendants
  descendants="$(collect_descendants "${pid}" | awk 'NF && !seen[$0]++' || true)"

  if [[ -n "${descendants}" ]]; then
    log "停止 ${name}（PID=${pid}，子进程: ${descendants//$'\n'/ }）..."
    while read -r child_pid; do
      [[ -n "${child_pid}" ]] && kill "${child_pid}" >/dev/null 2>&1 || true
    done <<< "${descendants}"
  else
    log "停止 ${name}（PID=${pid}）..."
  fi

  kill "${pid}" >/dev/null 2>&1 || true
  local i
  for i in $(seq 1 20); do
    if ! is_pid_running "${pid}" && ! any_pid_in_list_running <<< "${descendants}"; then
      rm -f "${pid_file}"
      log "${name} 已停止。"
      return 0
    fi
    sleep 0.5
  done

  warn "${name} 未在预期时间内退出，执行强制终止。"
  if [[ -n "${descendants}" ]]; then
    while read -r child_pid; do
      [[ -n "${child_pid}" ]] && kill -9 "${child_pid}" >/dev/null 2>&1 || true
    done <<< "${descendants}"
  fi
  kill -9 "${pid}" >/dev/null 2>&1 || true
  rm -f "${pid_file}"
}

print_status() {
  local bpid fpid child_pids
  bpid="$(read_pid "${BACKEND_PID_FILE}")"
  fpid="$(read_pid "${FRONTEND_PID_FILE}")"

  if [[ -n "${bpid}" ]] && is_pid_running "${bpid}"; then
    child_pids="$(collect_descendants "${bpid}" | awk 'NF && !seen[$0]++' | paste -sd',' - || true)"
    if [[ -n "${child_pids}" ]]; then
      printf '[OK] backend running  launcherPID=%s  childPID=%s  URL=http://127.0.0.1:%s (managed)\n' "${bpid}" "${child_pids}" "${BACKEND_PORT}"
    else
      printf '[OK] backend running  launcherPID=%s  URL=http://127.0.0.1:%s (managed)\n' "${bpid}" "${BACKEND_PORT}"
    fi
  elif backend_alive; then
    printf '[OK] backend running  URL=http://127.0.0.1:%s (unmanaged)\n' "${BACKEND_PORT}"
  else
    printf '[NO] backend stopped\n'
  fi

  if [[ -n "${fpid}" ]] && is_pid_running "${fpid}"; then
    printf '[OK] frontend running PID=%s  URL=http://127.0.0.1:%s (managed)\n' "${fpid}" "${FRONTEND_PORT}"
  elif frontend_alive; then
    printf '[OK] frontend running URL=http://127.0.0.1:%s (unmanaged)\n' "${FRONTEND_PORT}"
  else
    printf '[NO] frontend stopped\n'
  fi

  printf 'logs: %s , %s\n' "${BACKEND_LOG}" "${FRONTEND_LOG}"
}

start_all() {
  need_cmd curl
  need_cmd npm
  load_env
  start_backend
  wait_backend_ready
  start_frontend
  wait_frontend_ready
  print_status
}

stop_all() {
  stop_by_pid_file "frontend" "${FRONTEND_PID_FILE}"
  stop_by_pid_file "backend" "${BACKEND_PID_FILE}"
}

show_logs() {
  mkdir -p "${RUN_DIR}"
  touch "${BACKEND_LOG}" "${FRONTEND_LOG}"
  log "按 Ctrl+C 退出日志查看。"
  tail -n 120 -f "${BACKEND_LOG}" "${FRONTEND_LOG}"
}

main() {
  local action="${1:-start}"
  case "${action}" in
    start)
      start_all
      ;;
    stop)
      stop_all
      ;;
    restart)
      stop_all
      start_all
      ;;
    status)
      print_status
      ;;
    logs)
      show_logs
      ;;
    *)
      err "未知参数: ${action}"
      err "可用参数: start | stop | restart | status | logs"
      exit 1
      ;;
  esac
}

main "${1:-start}"

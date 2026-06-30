#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${RAG_ENV_FILE:-scripts/.env.rag.local}"
if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
QDRANT_URL="${RAG_VECTOR_ENDPOINT:-http://127.0.0.1:6333}"
CURL_NO_PROXY="${CURL_NO_PROXY:-*}"
CODEX_QA_ENABLED="${CODEX_QA_ENABLED:-true}"
CODEX_QA_COMMAND="${CODEX_QA_COMMAND:-codex}"
CODEX_QA_MODEL="${CODEX_QA_MODEL:-gpt-5.4-mini}"
QA_CHECK_TIMEOUT_SECONDS="${QA_CHECK_TIMEOUT_SECONDS:-240}"
CHECK_USERNAME="${CHECK_USERNAME:-20220001}"
CHECK_PASSWORD="${CHECK_PASSWORD:-123456}"
QA_CHECK_QUESTION="${QA_CHECK_QUESTION:-你是什么模型？请简短回答。}"

pass() {
  printf '[OK] %s\n' "$1"
}

fail() {
  printf '[FAIL] %s\n' "$1"
}

info() {
  printf '[INFO] %s\n' "$1"
}

if [[ "${RAG_ENABLED:-}" == "true" ]]; then
  pass "RAG_ENABLED=true"
else
  fail "RAG_ENABLED 未设置为 true"
fi

if [[ "${CODEX_QA_ENABLED:-}" == "true" ]]; then
  pass "CODEX_QA_ENABLED=true"
  if command -v "${CODEX_QA_COMMAND:-codex}" >/dev/null 2>&1; then
    pass "Codex 命令可用：${CODEX_QA_COMMAND:-codex}"
  else
    fail "Codex 命令不可用：${CODEX_QA_COMMAND:-codex}"
  fi
else
  fail "CODEX_QA_ENABLED 未设置为 true"
fi

if [[ -n "${RAG_LLM_API_KEY:-}" && "${RAG_LLM_API_KEY}" != "__REDACTED_SET_LOCALLY__" ]]; then
  pass "RAG_LLM_API_KEY 已设置（API 降级链路可用）"
else
  fail "RAG_LLM_API_KEY 未设置或仍为占位符（Codex 不可用时只能回退关键词匹配）"
fi

if curl --noproxy "${CURL_NO_PROXY}" -fsS "${QDRANT_URL}/collections" >/dev/null; then
  pass "Qdrant 可访问：${QDRANT_URL}"
else
  fail "Qdrant 不可访问：${QDRANT_URL}"
fi

LOGIN_RESP="$(curl --noproxy "${CURL_NO_PROXY}" -fsS -X POST "${BASE_URL}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${CHECK_USERNAME}\",\"password\":\"${CHECK_PASSWORD}\"}" 2>/dev/null || true)"

if [[ "${LOGIN_RESP}" == *'"code":0'* ]]; then
  pass "后端可访问且测试账号可登录：${BASE_URL}"
else
  fail "后端不可访问：${BASE_URL}"
  info "启动本地服务可执行：./scripts/dev-one-click.sh restart"
  exit 0
fi

TOKEN=""
if command -v python3 >/dev/null 2>&1; then
  TOKEN="$(LOGIN_RESP="${LOGIN_RESP}" python3 - <<'PY'
import json
import os

try:
    payload = json.loads(os.environ["LOGIN_RESP"])
    print(payload.get("data", {}).get("token", ""))
except Exception:
    print("")
PY
)"
fi

if [[ -n "${TOKEN}" ]]; then
  if command -v python3 >/dev/null 2>&1; then
    QA_PAYLOAD="$(QA_CHECK_QUESTION="${QA_CHECK_QUESTION}" python3 - <<'PY'
import json
import os

print(json.dumps({"question": os.environ["QA_CHECK_QUESTION"], "history": []}, ensure_ascii=False))
PY
)"
  else
    QA_PAYLOAD="{\"question\":\"${QA_CHECK_QUESTION}\",\"history\":[]}"
  fi

  QA_RESP="$(curl --no-buffer --noproxy "${CURL_NO_PROXY}" --max-time "${QA_CHECK_TIMEOUT_SECONDS}" -fsS -X POST "${BASE_URL}/api/v1/kb/qa/stream" \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${TOKEN}" \
    -d "${QA_PAYLOAD}" 2>/dev/null || true)"
  if [[ "${QA_RESP}" == *"event:answer"* || "${QA_RESP}" == *"event: answer"* ]]; then
    pass "智能问答流式接口可用：POST /api/v1/kb/qa/stream"
    if [[ "${QA_RESP}" == *"gpt-5.4-mini"* ]]; then
      pass "流式路由内部主链路配置符合预期"
    else
      fail "流式路由未返回预期主链路配置，请检查 CODEX_QA_MODEL"
    fi
    if command -v python3 >/dev/null 2>&1; then
      QA_RESP="${QA_RESP}" python3 - <<'PY'
import json
import os

blocks = [block.strip() for block in os.environ["QA_RESP"].split("\n\n") if block.strip()]
for block in blocks:
    event = "message"
    data_lines = []
    for line in block.splitlines():
        if line.startswith("event:"):
            event = line.split(":", 1)[1].strip()
        elif line.startswith("data:"):
            data_lines.append(line.split(":", 1)[1].strip())
    raw = "\n".join(data_lines)
    if event == "route":
        print("[INFO] 流式路由事件：已返回（内部链路细节已隐藏）")
    if event == "status":
        print("[INFO] 流式状态事件：" + raw[:240])
    if event == "answer":
        try:
            payload = json.loads(raw)
            answer = payload.get("answer", "")
        except Exception:
            answer = raw
        print("[INFO] 问答 stream 回答：" + answer[:180])
PY
    fi
  else
    fail "智能问答流式接口调用失败"
  fi
else
  fail "无法解析登录 token，跳过智能问答 smoke test"
fi

info "验收 Codex 主链路：脚本应看到 route/status/answer 事件；后端日志应出现 'codex qa completed'"
info "网页验收：学生端智能问答不显示模型、Codex、降级链路或生成路径，只显示自然的 AI RAG 回答与依据来源"
info "如需重建索引：./scripts/rag-reindex.sh"

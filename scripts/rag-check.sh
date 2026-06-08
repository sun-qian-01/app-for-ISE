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

if [[ -n "${RAG_LLM_API_KEY:-}" && "${RAG_LLM_API_KEY}" != "__REDACTED_SET_LOCALLY__" ]]; then
  pass "RAG_LLM_API_KEY 已设置"
else
  fail "RAG_LLM_API_KEY 未设置或仍为占位符"
fi

if curl --noproxy "${CURL_NO_PROXY}" -fsS "${QDRANT_URL}/collections" >/dev/null; then
  pass "Qdrant 可访问：${QDRANT_URL}"
else
  fail "Qdrant 不可访问：${QDRANT_URL}"
fi

if curl --noproxy "${CURL_NO_PROXY}" -fsS "${BASE_URL}/api/v1/auth/me" >/dev/null; then
  pass "后端可访问：${BASE_URL}"
else
  fail "后端不可访问：${BASE_URL}"
fi

info "如需重建索引：./scripts/rag-reindex.sh"

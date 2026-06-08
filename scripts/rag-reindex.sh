#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# RAG 索引重建脚本
#
# 作用：
# 1) 登录本地后端（默认 teacher001）获取 Bearer Token
# 2) 调用 /api/v1/kb/rag/reindex 触发知识库文章重建向量索引
#
# 使用前提：
# - 后端已启动（默认 http://127.0.0.1:8080）
# - 后端已配置好 RAG 环境变量（RAG_ENABLED / LLM / EMBED / QDRANT）
# - 已有可用管理员账号（默认 teacher001/123456）
#
# 可覆盖参数（环境变量）：
# - BASE_URL            默认 http://127.0.0.1:8080
# - LOGIN_USERNAME      默认 teacher001
# - LOGIN_PASSWORD      默认 123456
# - CURL_NO_PROXY       默认 *，避免本机 127.0.0.1 请求被 http_proxy/https_proxy 转发
# -----------------------------------------------------------------------------

ENV_FILE="${RAG_ENV_FILE:-scripts/.env.rag.local}"
if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
LOGIN_USERNAME="${LOGIN_USERNAME:-teacher001}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-123456}"
CURL_NO_PROXY="${CURL_NO_PROXY:-*}"

echo "[1/3] 登录获取 token: ${LOGIN_USERNAME} @ ${BASE_URL}"
LOGIN_RESP="$(curl --noproxy "${CURL_NO_PROXY}" -sS -X POST "${BASE_URL}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${LOGIN_USERNAME}\",\"password\":\"${LOGIN_PASSWORD}\"}")"

TOKEN="$(printf '%s' "${LOGIN_RESP}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
if [[ -z "${TOKEN}" ]]; then
  echo "登录失败，响应如下："
  echo "${LOGIN_RESP}"
  exit 1
fi

echo "[2/3] 调用 /api/v1/kb/rag/reindex"
REINDEX_RESP="$(curl --noproxy "${CURL_NO_PROXY}" -sS -X POST "${BASE_URL}/api/v1/kb/rag/reindex" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json')"

CODE="$(printf '%s' "${REINDEX_RESP}" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')"
if [[ "${CODE}" != "0" ]]; then
  echo "重建失败，响应如下："
  echo "${REINDEX_RESP}"
  exit 1
fi

INDEXED="$(printf '%s' "${REINDEX_RESP}" | sed -n 's/.*"indexedChunks":\([0-9]*\).*/\1/p')"

echo "[3/3] 完成"
echo "已重建 chunk 数：${INDEXED:-unknown}"
echo "完整响应：${REINDEX_RESP}"

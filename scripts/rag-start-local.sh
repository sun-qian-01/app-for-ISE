#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# 本地一键启动（带 RAG 环境变量）
#
# 作用：
# - 以当前 shell 导出的变量启动后端，默认开启 RAG
# - 不修改系统配置，不需要 sudo
#
# 推荐做法：
# 1) 将私有变量写入 scripts/.env.rag.local（已被 .gitignore 忽略）
# 2) 执行本脚本，脚本会自动加载该文件
#
# 可覆盖变量：
# - RAG_ENABLED (默认 true)
# - CODEX_QA_ENABLED / CODEX_QA_COMMAND / CODEX_QA_MODEL / CODEX_QA_WORKDIR / CODEX_QA_TIMEOUT_MS
# - RAG_LLM_BASE_URL / RAG_LLM_API_KEY / RAG_LLM_MODEL
# - RAG_EMBED_BASE_URL / RAG_EMBED_API_KEY / RAG_EMBED_MODEL
# - RAG_VECTOR_ENDPOINT / RAG_VECTOR_API_KEY / RAG_VECTOR_COLLECTION / RAG_VECTOR_DIMENSION
# -----------------------------------------------------------------------------

ENV_FILE="${RAG_ENV_FILE:-scripts/.env.rag.local}"
if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
  echo "已加载本地 RAG 环境文件：${ENV_FILE}"
fi

export RAG_ENABLED="${RAG_ENABLED:-true}"
export CODEX_QA_ENABLED="${CODEX_QA_ENABLED:-true}"
export CODEX_QA_COMMAND="${CODEX_QA_COMMAND:-codex}"
export CODEX_QA_MODEL="${CODEX_QA_MODEL:-gpt-5.4-mini}"
export CODEX_QA_WORKDIR="${CODEX_QA_WORKDIR:-$(pwd)}"
export CODEX_QA_TIMEOUT_MS="${CODEX_QA_TIMEOUT_MS:-180000}"

# 若未单独设置 embedding 地址/密钥，默认复用 LLM 配置。
export RAG_EMBED_BASE_URL="${RAG_EMBED_BASE_URL:-${RAG_LLM_BASE_URL:-https://gmn.chuangzuoli.com/v1}}"
export RAG_EMBED_API_KEY="${RAG_EMBED_API_KEY:-${RAG_LLM_API_KEY:-}}"
export RAG_EMBED_MODEL="${RAG_EMBED_MODEL:-text-embedding-3-small}"

export RAG_VECTOR_ENDPOINT="${RAG_VECTOR_ENDPOINT:-http://127.0.0.1:6333}"
export RAG_VECTOR_COLLECTION="${RAG_VECTOR_COLLECTION:-kb_article_chunks_v1}"
export RAG_VECTOR_DIMENSION="${RAG_VECTOR_DIMENSION:-1536}"

if [[ "${CODEX_QA_ENABLED}" == "true" ]] && ! command -v "${CODEX_QA_COMMAND}" >/dev/null 2>&1; then
  echo "警告：CODEX_QA_COMMAND=${CODEX_QA_COMMAND} 不可用，将尝试走 API 降级链路。"
fi

if [[ -z "${RAG_LLM_API_KEY:-}" || "${RAG_LLM_API_KEY}" == "__REDACTED_SET_LOCALLY__" ]]; then
  echo "警告：RAG_LLM_API_KEY 未设置或仍为占位符；若 Codex 不可用，问答将降级回关键词匹配。"
fi

echo "启动后端中（RAG_ENABLED=${RAG_ENABLED}, CODEX_QA_ENABLED=${CODEX_QA_ENABLED}, CODEX_QA_MODEL=${CODEX_QA_MODEL}）..."
if [[ -x "./mvnw" ]]; then
  ./mvnw spring-boot:run
elif command -v mvn >/dev/null 2>&1; then
  mvn spring-boot:run
else
  echo "错误：未找到 Maven。请安装 mvn 或在项目根目录提供 ./mvnw。"
  exit 1
fi

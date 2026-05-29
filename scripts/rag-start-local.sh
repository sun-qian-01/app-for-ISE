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
# 1) 复制 docs/backend/rag-env-example.md 里的变量到本机私有 env 文件
# 2) source 该 env 文件
# 3) 执行本脚本
#
# 可覆盖变量：
# - RAG_ENABLED (默认 true)
# - RAG_LLM_BASE_URL / RAG_LLM_API_KEY / RAG_LLM_MODEL
# - RAG_EMBED_BASE_URL / RAG_EMBED_API_KEY / RAG_EMBED_MODEL
# - RAG_VECTOR_ENDPOINT / RAG_VECTOR_API_KEY / RAG_VECTOR_COLLECTION / RAG_VECTOR_DIMENSION
# -----------------------------------------------------------------------------

export RAG_ENABLED="${RAG_ENABLED:-true}"

# 若未单独设置 embedding 地址/密钥，默认复用 LLM 配置。
export RAG_EMBED_BASE_URL="${RAG_EMBED_BASE_URL:-${RAG_LLM_BASE_URL:-https://gmn.chuangzuoli.com/v1}}"
export RAG_EMBED_API_KEY="${RAG_EMBED_API_KEY:-${RAG_LLM_API_KEY:-}}"
export RAG_EMBED_MODEL="${RAG_EMBED_MODEL:-text-embedding-3-small}"

export RAG_VECTOR_ENDPOINT="${RAG_VECTOR_ENDPOINT:-http://127.0.0.1:6333}"
export RAG_VECTOR_COLLECTION="${RAG_VECTOR_COLLECTION:-kb_article_chunks_v1}"
export RAG_VECTOR_DIMENSION="${RAG_VECTOR_DIMENSION:-1536}"

if [[ -z "${RAG_LLM_API_KEY:-}" ]]; then
  echo "警告：RAG_LLM_API_KEY 未设置，RAG 调用将降级回关键词问答。"
fi

echo "启动后端中（RAG_ENABLED=${RAG_ENABLED}）..."
if [[ -x "./mvnw" ]]; then
  ./mvnw spring-boot:run
elif command -v mvn >/dev/null 2>&1; then
  mvn spring-boot:run
else
  echo "错误：未找到 Maven。请安装 mvn 或在项目根目录提供 ./mvnw。"
  exit 1
fi

# RAG 环境变量示例

部署机建议配置（systemd Environment 或 `.env`）：

```bash
# 是否启用 RAG
RAG_ENABLED=true

# LLM（对齐 api-usage.md）
RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1
RAG_LLM_API_KEY=sk-xxxxxxxx
RAG_LLM_MODEL=gpt-5.4

# 向量库（示例以 Qdrant 为例）
RAG_VECTOR_PROVIDER=qdrant
RAG_VECTOR_ENDPOINT=http://127.0.0.1:6333
RAG_VECTOR_API_KEY=
RAG_VECTOR_COLLECTION=kb_article_chunks_v1

# 检索参数
RAG_TOP_K=20
RAG_MIN_SCORE=0.35
RAG_MAX_CONTEXT_CHUNKS=8
```

注意：

1. `RAG_LLM_API_KEY` 不要写入 git 仓库。
2. 若多环境部署，请按环境分别配置 key。
3. 建议在服务启动时打印“是否启用 RAG”与模型名，不打印 key。

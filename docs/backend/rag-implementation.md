# RAG 实施说明（已落地）

本文描述当前仓库已实现的最小可运行 RAG 链路，以及如何启动与重建索引。

## 1. 已实现内容

1. `POST /api/v1/kb/qa` 增加 RAG 分支（默认关闭）：
   - 开启且配置齐全时：`Embedding -> Qdrant 检索 -> Responses 生成`。
   - 开启但未检索到知识库片段时：仍调用 Responses 生成通用回答，返回空 `sources` 和低置信度；具体学院政策问题必须提示缺少可靠依据。
   - 上游失败时自动降级回原关键词匹配逻辑，保证演示可用。
2. 新增管理员接口：`POST /api/v1/kb/rag/reindex`
   - 角色限制：`teacher_admin` / `college_leader` / `system_admin`
   - 作用：重建已发布文章的向量索引。
3. 新增脚本：
   - `scripts/rag-start-local.sh`：本地带 RAG 环境变量启动。
   - `scripts/rag-reindex.sh`：登录后触发重建索引。

## 2. 新增后端模块

目录：`src/main/java/com/ise/platform/modules/kb/rag`

- `KbRagProperties`：读取 RAG 环境变量。
- `RagHttpClient`：统一 HTTP 调用（LLM/Qdrant）。
- `EmbeddingClient`：调用 `/embeddings`。
- `QdrantClient`：集合创建、向量 upsert、向量检索。
- `LlmResponsesClient`：按 `api-usage.md` 结构调用 `/responses`。
- `RagTextChunker`：按中文语义切块。
- `KbIndexerService`：从 `kb_article` 构建 chunk 并写入向量库。
- `KbRagService`：问答主链路与 sources 聚合。

## 3. 环境变量

建议参考 `docs/backend/rag-env-example.md`，至少包含：

- `RAG_ENABLED=true`
- `RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1`
- `RAG_LLM_API_KEY=...`
- `RAG_LLM_MODEL=gpt-5.2`
- `RAG_EMBED_MODEL=text-embedding-3-small`
- `RAG_VECTOR_ENDPOINT=http://127.0.0.1:6333`
- `RAG_VECTOR_COLLECTION=kb_article_chunks_v1`
- `RAG_VECTOR_DIMENSION=1536`

说明：
- 若未设置 `RAG_EMBED_BASE_URL/RAG_EMBED_API_KEY`，默认复用 LLM 配置。
- `RAG_ENABLED=true` 但关键配置缺失时，服务会自动走旧逻辑，不会中断 `kb/qa`。
- 旧逻辑不是大模型调用，只做问候/身份固定回复与已发布知识条目的关键词匹配；只有明显追问（如“那截止时间呢？”）才会使用最近历史辅助匹配，避免上一轮主题污染新问题。

## 4. 本地运行步骤

1. 启动 Qdrant（本机示例）
```bash
docker run -d --name qdrant -p 6333:6333 qdrant/qdrant
```

2. 导出 RAG 变量后启动后端
```bash
export RAG_ENABLED=true
export RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1
export RAG_LLM_API_KEY=<your-key>
export RAG_LLM_MODEL=gpt-5.2
export RAG_VECTOR_ENDPOINT=http://127.0.0.1:6333
./scripts/rag-start-local.sh
```

3. 重建索引
```bash
./scripts/rag-reindex.sh
```

## 5. 兼容与降级

- 对前端保持原接口：`POST /api/v1/kb/qa` 与原响应结构不变。
- 当向量库或大模型异常时，不抛给前端，自动回退关键词匹配。
- 因此你可以先灰度开启 RAG，再逐步观察效果。
- 前端若看到空 `sources`，应将回答视为无引用来源的低置信度回答；只有返回来源时才代表答案基于知识库证据。

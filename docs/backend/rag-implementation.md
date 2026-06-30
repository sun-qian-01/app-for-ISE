# RAG 实施说明（已落地）

本文描述当前仓库已实现的最小可运行 RAG 链路，以及如何启动与重建索引。

## 1. 已实现内容

1. `POST /api/v1/kb/qa` 增加 Codex-first 问答链路：
   - 优先使用部署机上的 `codex exec` 生成回答，默认模型为 `gpt-5.4-mini`。
   - Codex 会收到当前问题、最近历史和后端检索出的知识库证据；回答仍保持原接口结构。
   - Codex 失败或未返回文本时，自动降级到原 Responses API，再失败才回退关键词匹配。
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
- `CodexQaClient`：调用部署机上的 `codex exec`，从 JSONL 输出中提取最终回答。
- `FallbackQaAnswerClient`：Codex 优先，Responses API 作为降级链路。
- `RagTextChunker`：按中文语义切块。
- `KbIndexerService`：从 `kb_article` 构建 chunk 并写入向量库。
- `KbRagService`：问答主链路与 sources 聚合。

## 3. 环境变量

建议参考 `docs/backend/rag-env-example.md`，至少包含：

- `RAG_ENABLED=true`
- `CODEX_QA_ENABLED=true`
- `CODEX_QA_COMMAND=codex`
- `CODEX_QA_MODEL=gpt-5.4-mini`
- `CODEX_QA_WORKDIR=/opt/app-for-ise`
- `CODEX_QA_TIMEOUT_MS=180000`
- `RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1`
- `RAG_LLM_API_KEY=...`
- `RAG_LLM_MODEL=gpt-5.4`
- `RAG_EMBED_MODEL=text-embedding-3-small`
- `RAG_VECTOR_ENDPOINT=http://127.0.0.1:6333`
- `RAG_VECTOR_COLLECTION=kb_article_chunks_v1`
- `RAG_VECTOR_DIMENSION=1536`

说明：
- 若未设置 `RAG_EMBED_BASE_URL/RAG_EMBED_API_KEY`，默认复用 LLM 配置。
- `CODEX_QA_ENABLED=true` 时，服务优先拉起部署机 Codex；Codex 不可用或超时时再走 API 降级链路。
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
export CODEX_QA_ENABLED=true
export CODEX_QA_COMMAND=codex
export CODEX_QA_MODEL=gpt-5.4-mini
export CODEX_QA_WORKDIR=/opt/app-for-ise
export CODEX_QA_TIMEOUT_MS=180000
export RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1
export RAG_LLM_API_KEY=<your-key>
export RAG_LLM_MODEL=gpt-5.4
export RAG_VECTOR_ENDPOINT=http://127.0.0.1:6333
./scripts/rag-start-local.sh
```

3. 重建索引
```bash
./scripts/rag-reindex.sh
```

## 5. 兼容与降级

- 对前端保持原接口：`POST /api/v1/kb/qa` 与原响应结构不变。
- 学生端智能问答优先使用流式接口：`POST /api/v1/kb/qa/stream`，会先返回 `route/status` 事件，再返回最终 `answer` 事件。
- 当 Codex、向量库或大模型异常时，不抛给前端，自动回退到下一条可用链路。
- 因此你可以先灰度开启 RAG，再逐步观察效果。
- 前端若看到空 `sources`，应将回答视为无引用来源的低置信度回答；只有返回来源时才代表答案基于知识库证据。

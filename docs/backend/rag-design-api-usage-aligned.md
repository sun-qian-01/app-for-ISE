# RAG 方案（基于 `api-usage.md` 精确化）

本文在现有平台（`/api/v1/kb/*`）基础上，结合 `api-usage.md` 的 GPT 调用方式（`/v1/responses` + `input[]`）给出可落地设计。

---

## 1. 对齐目标

1. 继续保留现有 `POST /api/v1/kb/qa`，但内部从“关键词匹配”升级为“向量检索 + LLM 生成”。
2. 对知识库真实附录（PDF/DOCX）建立可增量索引。
3. 回答必须带来源条目（`articleId/title/sourceUrl`），保持当前前端 `sources` 渲染兼容。
4. 大模型调用严格按 `api-usage.md` 请求格式。

---

## 2. 与现有接口兼容策略

### 2.1 保留接口

- 已有接口：`POST /api/v1/kb/qa`
- 请求体沿用：
  - `question: string`
  - `categoryId?: long`
- 响应体沿用 `KbDto.QaResponse`：
  - `answer: string`
  - `sources: QaSource[]`
  - `confidence: double`

### 2.2 扩展字段（可选）

建议在不破坏现有前端的前提下，新增可选字段：

- `retrievalLatencyMs: long`
- `generationLatencyMs: long`
- `traceId: string`
- `fallback: boolean`（是否走降级）

---

## 3. 数据与索引模型

## 3.1 文章来源

来自当前 `kb_article`：

- `id`
- `title`
- `summary`
- `standard_answer`（当前正文主要来源）
- `source_file_id`
- `source_file_name`
- `publish_status`
- `updated_at`

## 3.2 索引粒度

每篇文章切分为若干 chunk（推荐 500~800 中文字，overlap 80~120 字）。

每个 chunk payload：

- `chunk_id`（`articleId + seq`）
- `article_id`
- `title`
- `category_label`
- `version_no`
- `source_file_id`
- `source_file_name`
- `publish_status`
- `updated_at`
- `chunk_text`
- `embedding_model`

## 3.3 向量库

推荐：Qdrant（便于 payload filter）。

Collection：`kb_article_chunks_v1`

---

## 4. 检索流程（服务端）

1. 参数校验：`question` 非空。
2. 查询 embedding（query vector）。
3. 向量检索 TopK（建议 20），并过滤：
   - `publish_status = published`
4. 组装证据上下文（优先高分 chunk，截断总 token）。
5. 生成阶段调用 GPT Responses API。
6. 返回 `QaResponse` + `sources`（去重后最多 3~5 条）。
7. 若检索为空或置信度低：返回“未检索到可靠依据”。

---

## 5. GPT 调用规范（严格对齐 `api-usage.md`）

> 来自 `api-usage.md`：
> - URL: `https://gmn.chuangzuoli.com/v1/responses`
> - Header: `Authorization: Bearer sk-xxxx`, `Content-Type: application/json`
> - Body: `input` 使用数组消息结构

### 5.1 建议请求模板（后端）

```json
{
  "model": "gpt-5.4",
  "input": [
    {
      "type": "message",
      "role": "developer",
      "content": [
        {
          "type": "input_text",
          "text": "你是学院知识库问答助手。仅基于提供的证据回答；若证据不足，明确回复“未检索到可靠依据”。输出两段：1)回答 2)依据条目ID列表。"
        }
      ]
    },
    {
      "type": "message",
      "role": "user",
      "content": [
        {
          "type": "input_text",
          "text": "问题：<QUESTION>\\n\\n证据：\\n[articleId=6] ...\\n[articleId=9] ..."
        }
      ]
    }
  ]
}
```

### 5.2 服务端实现要点

1. 禁止前端直连该第三方接口，密钥仅在后端持有。
2. 环境变量配置：
   - `RAG_LLM_BASE_URL=https://gmn.chuangzuoli.com/v1`
   - `RAG_LLM_API_KEY=sk-xxx`
   - `RAG_LLM_MODEL=gpt-5.4`
3. 超时与重试：
   - connect/read timeout 8~15s
   - 429/5xx 指数退避重试 1~2 次
4. 解析时若返回异常，降级为“检索摘要回答”。

---

## 6. 新增后端模块建议

## 6.1 包结构

- `modules/kb/rag/KbRagService`
- `modules/kb/rag/EmbeddingClient`
- `modules/kb/rag/LlmResponsesClient`
- `modules/kb/rag/VectorStoreClient`
- `modules/kb/rag/KbIndexerService`

## 6.2 配置对象

- `KbRagProperties`
  - `enabled`
  - `topK`
  - `minScore`
  - `llm.baseUrl`
  - `llm.apiKey`
  - `llm.model`
  - `vector.provider`
  - `vector.endpoint`
  - `vector.apiKey`
  - `vector.collection`

---

## 7. 索引任务与一致性

## 7.1 触发策略

1. 文章发布/更新后，写入 `kb_index_task`（推荐新增表）。
2. 异步 worker 消费任务进行 upsert。
3. 失败任务记录错误并可重试。

## 7.2 推荐新增表

- `kb_index_task`
  - `id`
  - `article_id`
  - `op_type`（upsert/delete）
  - `status`（pending/running/success/failed）
  - `retry_count`
  - `error_message`
  - `created_at`
  - `updated_at`

- `kb_qa_log`
  - `id`
  - `user_id`
  - `question`
  - `answer`
  - `sources_json`
  - `confidence`
  - `trace_id`
  - `created_at`

---

## 8. 安全与风控

1. API Key 不入库，不写前端，不进 git。
2. 记录 `traceId`，用于排查越权和幻觉问题。
3. Prompt 注入防护：
   - 上下文文本做长度与字符过滤
   - System/Developer 指令固定，不允许用户覆盖
4. 限流：
   - 按用户对 `POST /kb/qa` 做限流（如每分钟 20 次）

---

## 9. 质量门槛（验收）

1. 回答引用率 >= 95%（非 fallback 场景）。
2. 无依据问题返回“未检索到可靠依据”准确率 >= 90%。
3. P95 总延迟 <= 2.5s（不含首包冷启动）。
4. 索引任务失败率 <= 1%。

---

## 10. 落地顺序

1. 第 1 步：实现 `KbIndexerService + VectorStoreClient`，完成离线建索引。
2. 第 2 步：实现 `LlmResponsesClient`，将 `/kb/qa` 改成检索增强。
3. 第 3 步：新增 `kb_index_task/kb_qa_log` 与后台重试。
4. 第 4 步：灰度给 student 角色，观察日志与命中率后全量。

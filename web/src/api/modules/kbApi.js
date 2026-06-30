import client from "../client";
import { parseSseEvents } from "../../utils/sse";

export async function getKnowledgeList(params = {}) {
  return client.get("/kb/articles", { params });
}

export async function getKnowledgeArticleDetail(articleId) {
  return client.get(`/kb/articles/${articleId}`);
}

export async function getKnowledgeTemplates() {
  return client.get("/kb/templates");
}

export async function askKnowledgeQuestion(question, options = {}) {
  return client.post("/kb/qa", {
    question,
    history: options.history || [],
  });
}

export async function askKnowledgeQuestionStream(question, options = {}) {
  const token = localStorage.getItem("ise_token");
  const response = await fetch("/api/v1/kb/qa/stream", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({
      question,
      history: options.history || [],
    }),
  });

  if (!response.ok || !response.body) {
    throw new Error(`问答流式请求失败：HTTP ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  let finalAnswer = null;

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const splitAt = buffer.lastIndexOf("\n\n");
    if (splitAt < 0) {
      continue;
    }
    const ready = buffer.slice(0, splitAt + 2);
    buffer = buffer.slice(splitAt + 2);
    for (const item of parseSseEvents(ready)) {
      options.onEvent?.(item.event, item.data);
      if (item.event === "answer") {
        finalAnswer = item.data;
      }
    }
  }

  if (buffer.trim()) {
    for (const item of parseSseEvents(buffer + "\n\n")) {
      options.onEvent?.(item.event, item.data);
      if (item.event === "answer") {
        finalAnswer = item.data;
      }
    }
  }

  if (!finalAnswer) {
    throw new Error("问答流未返回最终答案");
  }
  return finalAnswer;
}

export { parseSseEvents };

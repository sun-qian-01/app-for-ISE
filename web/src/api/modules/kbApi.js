import client from "../client";

export async function getKnowledgeList(params = {}) {
  return client.get("/kb/articles", { params });
}

export async function getKnowledgeArticleDetail(articleId) {
  return client.get(`/kb/articles/${articleId}`);
}

export async function getKnowledgeTemplates() {
  return client.get("/kb/templates");
}

export async function askKnowledgeQuestion(question) {
  return client.post("/kb/qa", { question });
}

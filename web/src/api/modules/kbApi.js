import { fetchKnowledgeList, fetchKnowledgeTemplates } from "../../mocks/server";

export async function getKnowledgeList() {
  return fetchKnowledgeList();
}

export async function getKnowledgeTemplates() {
  return fetchKnowledgeTemplates();
}

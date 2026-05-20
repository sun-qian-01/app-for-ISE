import { fetchKnowledgeList } from "../../mocks/server";

export async function getKnowledgeList() {
  return fetchKnowledgeList();
}

import client from "../client";

export async function getMyPartyStages() {
  return client.get("/party/instances/me");
}

export async function getPartyTodos(params = {}) {
  return client.get("/party/todos", { params });
}

export async function reviewPartyStage(stageRecordId, action, comment) {
  return client.post(`/party/stage-records/${stageRecordId}/review`, { action, comment });
}

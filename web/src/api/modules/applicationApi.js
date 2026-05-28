import client from "../client";

export async function getMyApplications(params = {}) {
  return client.get("/applications/my", { params });
}

export async function createApplication(payload) {
  return client.post("/applications", payload);
}

export async function getPendingApplications(params = {}) {
  return client.get("/applications/approvals/pending", { params });
}

export async function approveApplication(applicationId, comment) {
  return client.post(`/applications/${applicationId}/approve`, { comment });
}

export async function rejectApplication(applicationId, comment) {
  return client.post(`/applications/${applicationId}/reject`, { comment });
}

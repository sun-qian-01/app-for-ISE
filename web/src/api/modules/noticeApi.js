import client from "../client";

export async function getMyNotices(params = {}) {
  return client.get("/notices/my", { params });
}

export async function getMyNoticeDetail(noticeId) {
  return client.get(`/notices/my/${noticeId}`);
}

export async function markNoticeRead(noticeId) {
  return client.post(`/notices/${noticeId}/read`);
}

export async function markAllNoticesRead() {
  return client.post("/notices/read-all");
}

export async function getNoticeList(params = {}) {
  return client.get("/notices", { params });
}

export async function createNotice(payload) {
  return client.post("/notices", payload);
}

import { fetchNotices } from "../../mocks/server";

export async function getMyNotices() {
  return fetchNotices();
}

export async function getNoticeList() {
  return fetchNotices();
}

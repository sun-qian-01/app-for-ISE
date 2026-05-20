import { fetchNotices } from "../../mocks/server";

export async function getMyNotices() {
  return fetchNotices();
}

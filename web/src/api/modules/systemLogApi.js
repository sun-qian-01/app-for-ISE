import { fetchSystemLogs } from "../../mocks/server";

export async function getSystemLogs() {
  return fetchSystemLogs();
}

import { fetchAuditLogs } from "../../mocks/server";

export async function getAuditLogs() {
  return fetchAuditLogs();
}

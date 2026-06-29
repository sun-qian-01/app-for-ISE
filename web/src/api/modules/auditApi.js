import client from "../client";

export async function getAuditLogs() {
  return client.get("/audit-logs");
}

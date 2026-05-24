import client from "../client";

export async function getStudentDashboard() {
  return client.get("/dashboard/student");
}

export async function getAdminDashboard() {
  return client.get("/dashboard/admin");
}

export async function getLeaderDashboard() {
  return client.get("/dashboard/leader");
}

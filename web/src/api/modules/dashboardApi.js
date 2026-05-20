import { fetchAdminDashboard, fetchStudentDashboard } from "../../mocks/server";

export async function getStudentDashboard() {
  return fetchStudentDashboard();
}

export async function getAdminDashboard() {
  return fetchAdminDashboard();
}

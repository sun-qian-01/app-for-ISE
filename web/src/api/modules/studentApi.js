import { fetchAdminStudents, fetchProfile } from "../../mocks/server";

export async function getMyProfile() {
  return fetchProfile();
}

export async function getStudentList() {
  return fetchAdminStudents();
}

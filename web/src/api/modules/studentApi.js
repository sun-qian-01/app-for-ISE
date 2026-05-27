import client from "../client";
import { batchRegisterStudents, updateProfile } from "../../mocks/server";

export async function getMyProfile() {
  return client.get("/students/me/profile");
}

export async function getMyGrowthRecords() {
  const profile = await getMyProfile();
  const studentId = profile?.student?.id;
  if (!studentId) {
    return [];
  }
  return client.get(`/students/${studentId}/growth-records`);
}

export async function getStudentList(params = {}) {
  return client.get("/students", { params });
}

export async function batchRegisterStudentsApi(rows) {
  return batchRegisterStudents(rows);
}

export async function updateMyProfileApi(payload) {
  return updateProfile(payload);
}

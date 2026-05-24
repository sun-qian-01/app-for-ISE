import client from "../client";

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

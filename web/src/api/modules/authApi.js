import client from "../client";

export async function loginApi(payload) {
  return client.post("/auth/login", {
    username: payload.username,
    password: payload.password,
  });
}

export async function meApi() {
  return client.get("/auth/me");
}

export async function logoutApi() {
  return client.post("/auth/logout");
}

export async function registerStudentApi(payload) {
  return client.post("/auth/register", {
    studentNo: payload.studentNo,
    name: payload.name,
    grade: payload.grade,
    major: payload.major,
    className: payload.className,
    phone: payload.phone,
    email: payload.email,
    politicalStatusLabel: payload.politicalStatusLabel,
    password: payload.password,
  });
}

export async function changePasswordApi(payload) {
  return client.post("/auth/password", {
    oldPassword: payload.oldPassword,
    newPassword: payload.newPassword,
  });
}

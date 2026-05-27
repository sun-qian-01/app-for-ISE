import client from "../client";
import { changePassword, registerStudent } from "../../mocks/server";

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
  return registerStudent(payload);
}

export async function changePasswordApi(payload) {
  return changePassword(payload);
}

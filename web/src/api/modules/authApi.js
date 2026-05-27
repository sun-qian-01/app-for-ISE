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

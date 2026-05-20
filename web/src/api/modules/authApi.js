import { mockLogin, mockMe } from "../../mocks/server";

export async function loginApi(payload) {
  return mockLogin(payload);
}

export async function meApi() {
  return mockMe();
}

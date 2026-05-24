import axios from "axios";

const client = axios.create({
  baseURL: "/api/v1",
  timeout: 10000,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem("ise_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload?.code !== 0) {
      const error = new Error(payload?.message || "请求失败");
      error.businessCode = payload?.code;
      error.status = response.status;
      error.requestId = payload?.requestId;
      throw error;
    }
    return payload.data;
  },
  (error) => {
    if (error?.response?.data) {
      const payload = error.response.data;
      const normalizedError = new Error(payload?.message || error.message || "请求失败");
      normalizedError.businessCode = payload?.code;
      normalizedError.status = error.response.status;
      normalizedError.requestId = payload?.requestId;
      normalizedError.raw = error;
      throw normalizedError;
    }
    if (error?.response?.status) {
      const normalizedError = new Error(error.message || "请求失败");
      normalizedError.status = error.response.status;
      normalizedError.raw = error;
      throw normalizedError;
    }
    throw error;
  },
);

export default client;

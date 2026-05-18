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
      error.requestId = payload?.requestId;
      throw error;
    }
    return payload.data;
  },
  (error) => {
    throw error;
  },
);

export default client;

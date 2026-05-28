import client from "../client";

export async function uploadFile(file, bizType) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("bizType", bizType);
  return client.post("/files/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}

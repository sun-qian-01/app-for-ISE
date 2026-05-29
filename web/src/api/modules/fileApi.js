import client from "../client";

export async function uploadFile(file, bizType) {
  const form = new FormData();
  form.append("file", file);
  return client.post("/files/upload", form, {
    params: { bizType },
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

import client from "../client";

export async function getPublicHonors() {
  return client.get("/honors/my");
}

export async function getHonorList() {
  return client.get("/honors");
}

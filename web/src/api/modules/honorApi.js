import { fetchHonors } from "../../mocks/server";

export async function getPublicHonors() {
  return fetchHonors();
}

export async function getHonorList() {
  return fetchHonors();
}

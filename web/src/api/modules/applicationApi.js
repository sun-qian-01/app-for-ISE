import { fetchApplications } from "../../mocks/server";

export async function getMyApplications() {
  return fetchApplications();
}

export async function getPendingApplications() {
  return fetchApplications();
}

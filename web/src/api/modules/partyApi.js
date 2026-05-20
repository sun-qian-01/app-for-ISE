import { fetchPartyStages } from "../../mocks/server";

export async function getMyPartyStages() {
  return fetchPartyStages();
}

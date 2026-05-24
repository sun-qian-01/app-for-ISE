import client from "../client";

export async function getDicts(types = []) {
  const params = {};
  if (types.length) {
    params.types = types.join(",");
  }
  return client.get("/dicts", { params });
}

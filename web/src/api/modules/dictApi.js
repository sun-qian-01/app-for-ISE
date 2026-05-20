import { dictionaries } from "../../constants/dictionaries";

export async function getDicts(types = []) {
  if (!types.length) return dictionaries;
  return Object.fromEntries(types.map((type) => [type, dictionaries[type] ?? []]));
}

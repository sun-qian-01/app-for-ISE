import { computed } from "vue";
import { dictionaries } from "../constants/dictionaries";

export function useDictionary(type) {
  const options = computed(() => dictionaries[type] ?? []);

  function getLabel(value, fallback = value) {
    return options.value.find((item) => item.value === value)?.label ?? fallback;
  }

  return {
    options,
    getLabel,
  };
}

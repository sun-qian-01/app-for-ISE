import { computed, ref } from "vue";
import { getDicts } from "../api/modules/dictApi";
import { dictionaries as fallbackDictionaries } from "../constants/dictionaries";

const loadedDictCache = new Map();

export function useDictionary(type) {
  const remoteOptions = ref([]);
  const options = computed(() => (remoteOptions.value.length ? remoteOptions.value : (fallbackDictionaries[type] ?? [])));

  if (type) {
    const cached = loadedDictCache.get(type);
    if (cached) {
      remoteOptions.value = cached;
    } else {
      getDicts([type])
        .then((payload) => {
          const loaded = payload[type];
          if (!Array.isArray(loaded)) {
            return;
          }
          loadedDictCache.set(type, loaded);
          remoteOptions.value = loaded;
        })
        .catch(() => {});
    }
  }

  function getLabel(value, fallback = value) {
    return options.value.find((item) => item.value === value)?.label ?? fallback;
  }

  return {
    options,
    getLabel,
  };
}

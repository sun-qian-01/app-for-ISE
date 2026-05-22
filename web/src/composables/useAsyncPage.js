import { ref } from "vue";

export function useAsyncPage(handler) {
  const loading = ref(false);
  const error = ref(false);
  const errorDetail = ref(null);

  async function run(...args) {
    loading.value = true;
    error.value = false;
    errorDetail.value = null;

    try {
      return await handler(...args);
    } catch (err) {
      console.error(err);
      error.value = true;
      errorDetail.value = err;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    error,
    errorDetail,
    run,
  };
}

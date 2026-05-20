<template>
  <div class="error-state">
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
    <p v-if="requestId" class="error-state__request-id">requestId: {{ requestId }}</p>
    <button v-if="retryable" class="button" type="button" @click="$emit('retry')">重试</button>
  </div>
</template>

<script setup>
defineEmits(["retry"]);

defineProps({
  title: {
    type: String,
    default: "加载失败",
  },
  description: {
    type: String,
    default: "当前页面数据获取失败，请稍后重试。",
  },
  requestId: {
    type: String,
    default: "",
  },
  retryable: {
    type: Boolean,
    default: true,
  },
});
</script>

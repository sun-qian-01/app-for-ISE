<template>
  <div class="loading-state">
    <div class="loading-state__spinner" />
    <p>{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: "数据加载中...",
  },
});
</script>

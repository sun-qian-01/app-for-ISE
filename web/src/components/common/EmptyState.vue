<template>
  <div class="empty-state">
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: "暂无数据",
  },
  description: {
    type: String,
    default: "当前筛选条件下没有可展示内容。",
  },
});
</script>

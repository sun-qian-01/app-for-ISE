<template>
  <section class="panel">
    <div class="section-head">
      <h2>系统日志</h2>
      <span class="pill">GET /system-logs</span>
    </div>
    <div class="stack">
      <article v-for="item in items" :key="item.requestId" class="log-item">
        <div class="record__meta">{{ item.levelLabel }} · {{ item.module }} · {{ item.requestId }}</div>
        <h3>{{ item.message }}</h3>
        <p>{{ item.path }}</p>
        <div class="log-detail">{{ item.detail }}</div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchSystemLogs } from "../../mocks/server";

const items = ref([]);

onMounted(async () => {
  items.value = await fetchSystemLogs();
});
</script>

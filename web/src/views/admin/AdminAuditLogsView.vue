<template>
  <section class="panel">
    <div class="section-head">
      <h2>审计日志</h2>
      <span class="pill">GET /audit-logs</span>
    </div>
    <div class="stack">
      <article v-for="item in items" :key="`${item.actor}-${item.time}`" class="log-item">
        <div class="record__meta">{{ item.time }} · {{ item.module }}</div>
        <h3>{{ item.actor }}</h3>
        <p>{{ item.action }}</p>
        <span class="pill">{{ item.result }}</span>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchAuditLogs } from "../../mocks/server";

const items = ref([]);

onMounted(async () => {
  items.value = await fetchAuditLogs();
});
</script>

<template>
  <section class="panel">
    <div class="section-head">
      <h2>审批处理</h2>
      <span class="pill">GET /applications/approvals/pending</span>
    </div>
    <div class="stack">
      <article v-for="item in items" :key="item.no" class="record">
        <div class="record__meta">{{ item.no }} · {{ item.createdAt }}</div>
        <h3>{{ item.typeLabel }}</h3>
        <p>用途：{{ item.purpose }}</p>
        <div class="tag-group">
          <span class="pill">{{ item.statusLabel }}</span>
          <span class="tag">{{ item.approver }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchApplications } from "../../mocks/server";

const items = ref([]);

onMounted(async () => {
  items.value = await fetchApplications();
});
</script>

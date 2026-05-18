<template>
  <section>
    <div class="grid grid--metrics">
      <article class="metric-card">
        <span>学生总数</span>
        <strong>{{ data.studentCount }}</strong>
      </article>
      <article class="metric-card">
        <span>待审批</span>
        <strong>{{ data.pendingApprovalCount }}</strong>
      </article>
      <article class="metric-card">
        <span>今日推送</span>
        <strong>{{ data.todayPushCount }}</strong>
      </article>
      <article class="metric-card">
        <span>风险预警</span>
        <strong>{{ data.riskCount }}</strong>
      </article>
    </div>
    <section class="panel">
      <div class="section-head">
        <h2>业务看板</h2>
        <span class="pill">GET /dashboard/admin</span>
      </div>
      <div class="info-list">
        <div v-for="[label, value] in data.board" :key="label">
          <span>{{ label }}</span>
          <strong>{{ value }}</strong>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { fetchAdminDashboard } from "../../mocks/server";

const data = reactive({
  studentCount: 0,
  pendingApprovalCount: 0,
  todayPushCount: 0,
  riskCount: 0,
  board: [],
});

onMounted(async () => {
  Object.assign(data, await fetchAdminDashboard());
});
</script>

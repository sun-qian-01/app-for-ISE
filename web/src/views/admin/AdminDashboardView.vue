<template>
  <section>
    <div class="grid grid--metrics">
      <MetricCard label="学生总数" :value="data.studentCount" />
      <MetricCard label="待审批" :value="data.pendingApprovalCount" />
      <MetricCard label="今日推送" :value="data.todayPushCount" />
      <MetricCard label="风险预警" :value="data.riskCount" />
    </div>
    <section class="panel">
      <PageHeader title="业务看板" api="GET /dashboard/admin" />
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
import MetricCard from "../../components/common/MetricCard.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import { getAdminDashboard } from "../../api/modules/dashboardApi";

const data = reactive({
  studentCount: 0,
  pendingApprovalCount: 0,
  todayPushCount: 0,
  riskCount: 0,
  board: [],
});

onMounted(async () => {
  Object.assign(data, await getAdminDashboard());
});
</script>

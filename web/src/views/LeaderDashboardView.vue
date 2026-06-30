<template>
  <div class="shell">
    <section class="panel">
      <div class="section-head">
        <h2>领导看板</h2>
      </div>
      <div class="grid grid--metrics">
        <MetricCard label="学生总数" :value="data.studentTotal" />
        <MetricCard label="党团流程在办" :value="data.partyProcessActive" />
        <MetricCard label="通知已读率" :value="`${Math.round((data.noticeReadRate || 0) * 100)}%`" text-mode />
        <MetricCard label="申请通过率" :value="`${Math.round((data.applicationApprovedRate || 0) * 100)}%`" text-mode />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import MetricCard from "../components/common/MetricCard.vue";
import { getLeaderDashboard } from "../api/modules/dashboardApi";

const data = reactive({
  studentTotal: 0,
  partyProcessActive: 0,
  noticeReadRate: 0,
  applicationApprovedRate: 0,
});

onMounted(async () => {
  Object.assign(data, await getLeaderDashboard());
});
</script>

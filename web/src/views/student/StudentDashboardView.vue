<template>
  <section>
    <div class="grid grid--metrics">
      <MetricCard label="待办事项" :value="data.todoCount" hint="材料、申请和通知催办" />
      <MetricCard label="未读通知" :value="data.unreadNoticeCount" hint="站内消息与微信提醒" />
      <MetricCard label="当前阶段" :value="data.currentPartyStage" hint="入党流程办理中" text-mode />
      <MetricCard label="成长记录" :value="data.growthCount" hint="竞赛、实践、志愿服务" />
    </div>
    <div class="grid grid--two">
      <section class="panel">
        <PageHeader title="快捷办理" description="常用事项入口，可直接跳转办理。" />
        <div class="shortcut-grid">
          <RouterLink class="shortcut" to="/student/kb">政策问答</RouterLink>
          <RouterLink class="shortcut" to="/student/party">查看党团流程</RouterLink>
          <RouterLink class="shortcut" to="/student/applications">申请在读证明</RouterLink>
          <RouterLink class="shortcut" to="/student/notices">查看定向通知</RouterLink>
        </div>
      </section>
      <section class="panel">
        <PageHeader title="使用提示" />
        <p class="hero__text">当前页面数据已接入后端接口，若状态变化可刷新查看最新结果。</p>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { RouterLink } from "vue-router";
import MetricCard from "../../components/common/MetricCard.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import { getStudentDashboard } from "../../api/modules/dashboardApi";

const data = reactive({
  todoCount: 0,
  unreadNoticeCount: 0,
  currentPartyStage: "",
  growthCount: 0,
});

onMounted(async () => {
  Object.assign(data, await getStudentDashboard());
});
</script>

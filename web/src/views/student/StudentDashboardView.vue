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
        <PageHeader title="快捷办理" api="GET /dashboard/student" />
        <div class="shortcut-grid">
          <RouterLink class="shortcut" to="/student/kb">政策问答</RouterLink>
          <RouterLink class="shortcut" to="/student/party">提交党团材料</RouterLink>
          <RouterLink class="shortcut" to="/student/applications">申请在读证明</RouterLink>
          <RouterLink class="shortcut" to="/student/notices">查看定向通知</RouterLink>
        </div>
      </section>
      <section class="panel">
        <PageHeader title="页面状态">
          <template #meta>
            <StatusTag label="Vue 版已接管" tone="success" />
          </template>
        </PageHeader>
        <p class="hero__text">这是从静态原型迁移出来的首个正式页面，后续会逐步替换为真实 API 数据。</p>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { RouterLink } from "vue-router";
import MetricCard from "../../components/common/MetricCard.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { fetchStudentDashboard } from "../../mocks/server";

const data = reactive({
  todoCount: 0,
  unreadNoticeCount: 0,
  currentPartyStage: "",
  growthCount: 0,
});

onMounted(async () => {
  Object.assign(data, await fetchStudentDashboard());
});
</script>

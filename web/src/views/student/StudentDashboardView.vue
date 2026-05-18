<template>
  <section>
    <div class="grid grid--metrics">
      <article class="metric-card">
        <span>待办事项</span>
        <strong>{{ data.todoCount }}</strong>
        <small>材料、申请和通知催办</small>
      </article>
      <article class="metric-card">
        <span>未读通知</span>
        <strong>{{ data.unreadNoticeCount }}</strong>
        <small>站内消息与微信提醒</small>
      </article>
      <article class="metric-card">
        <span>当前阶段</span>
        <strong class="metric-text">{{ data.currentPartyStage }}</strong>
        <small>入党流程办理中</small>
      </article>
      <article class="metric-card">
        <span>成长记录</span>
        <strong>{{ data.growthCount }}</strong>
        <small>竞赛、实践、志愿服务</small>
      </article>
    </div>
    <div class="grid grid--two">
      <section class="panel">
        <div class="section-head">
          <h2>快捷办理</h2>
          <span class="pill">GET /dashboard/student</span>
        </div>
        <div class="shortcut-grid">
          <RouterLink class="shortcut" to="/student/kb">政策问答</RouterLink>
          <RouterLink class="shortcut" to="/student/party">提交党团材料</RouterLink>
          <RouterLink class="shortcut" to="/student/applications">申请在读证明</RouterLink>
          <RouterLink class="shortcut" to="/student/notices">查看定向通知</RouterLink>
        </div>
      </section>
      <section class="panel">
        <div class="section-head">
          <h2>页面状态</h2>
          <span class="pill pill--success">Vue 版已接管</span>
        </div>
        <p class="hero__text">这是从静态原型迁移出来的首个正式页面，后续会逐步替换为真实 API 数据。</p>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { RouterLink } from "vue-router";
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

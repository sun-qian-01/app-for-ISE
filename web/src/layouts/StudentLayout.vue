<template>
  <div class="app-shell">
    <aside class="sidebar">
      <RouterLink class="brand" to="/student/dashboard">
        <span class="brand__mark">ISE</span>
        <span class="brand__text">学院综合服务平台</span>
      </RouterLink>
      <nav class="nav">
        <RouterLink v-for="item in navItems" :key="item.to" class="nav__item" :to="item.to">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>
    <main class="content">
      <header class="topbar">
        <div>
          <div class="eyebrow">学生端 · self 数据范围</div>
          <h1>你好，{{ authStore.user?.realName || "同学" }}</h1>
        </div>
        <div class="topbar__actions">
          <span class="pill pill--success">实名已绑定</span>
          <span class="pill">{{ authStore.roleCode }}</span>
          <RouterLink class="button" to="/login">返回登录页</RouterLink>
        </div>
      </header>
      <section class="panel panel--banner">
        <div class="section-head">
          <h2>当前访问上下文</h2>
          <span class="pill">GET /api/v1/auth/me</span>
        </div>
        <div class="scope-grid">
          <div class="scope-card">
            <span>角色代码</span>
            <strong>{{ authStore.roleCode }}</strong>
          </div>
          <div class="scope-card">
            <span>数据范围</span>
            <strong>self</strong>
          </div>
          <div class="scope-card">
            <span>当前首页接口</span>
            <strong>/dashboard/student</strong>
          </div>
        </div>
      </section>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, RouterView } from "vue-router";
import { useAuthStore } from "../stores/auth";

const authStore = useAuthStore();

const navItems = computed(() => [
  { label: "首页", to: "/student/dashboard" },
  { label: "智能问答", to: "/student/kb" },
  { label: "党团流程", to: "/student/party" },
  { label: "通知中心", to: "/student/notices" },
  { label: "院内申请", to: "/student/applications" },
  { label: "个人画像", to: "/student/profile" },
  { label: "奖励荣誉", to: "/student/honors" },
]);
</script>

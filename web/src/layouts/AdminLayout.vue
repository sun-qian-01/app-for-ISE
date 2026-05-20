<template>
  <div class="app-shell">
    <aside class="sidebar sidebar--admin">
      <RouterLink class="brand" to="/admin/dashboard">
        <span class="brand__mark">ADM</span>
        <span class="brand__text">业务管理台</span>
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
          <div class="eyebrow">管理端 · department 数据范围</div>
          <h1>学生工作与党团事务管理台</h1>
        </div>
        <div class="topbar__actions">
          <StatusTag :label="`角色：${roleLabel}`" tone="success" />
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
            <span>默认数据范围</span>
            <strong>department</strong>
          </div>
          <div class="scope-card">
            <span>当前首页接口</span>
            <strong>/dashboard/admin</strong>
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
import StatusTag from "../components/common/StatusTag.vue";
import { useDictionary } from "../composables/useDictionary";
import { useAuthStore } from "../stores/auth";

const authStore = useAuthStore();
const { getLabel } = useDictionary("role_code");

const navItems = computed(() => [
  { label: "概览", to: "/admin/dashboard" },
  { label: "学生画像", to: "/admin/students" },
  { label: "知识库", to: "/admin/kb" },
  { label: "党团流程", to: "/admin/party" },
  { label: "精准通知", to: "/admin/notices" },
  { label: "审批处理", to: "/admin/applications" },
  { label: "荣誉展示", to: "/admin/honors" },
  { label: "审计日志", to: "/admin/audit-logs" },
  { label: "系统日志", to: "/admin/system-logs" },
]);

const roleLabel = computed(() => getLabel(authStore.roleCode, authStore.roleCode));
</script>

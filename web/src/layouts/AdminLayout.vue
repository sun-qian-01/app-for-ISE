<template>
  <AppShell
    brand-to="/admin/dashboard"
    brand-mark="ADM"
    brand-text="业务管理台"
    :nav-items="navItems"
    eyebrow="管理端 · department 数据范围"
    title="学生工作与党团事务管理台"
    role-label-text="角色代码"
    role-value="authStore.roleCode"
    scope-label-text="默认数据范围"
    scope-value="department"
    home-api-value="/dashboard/admin"
    admin
  >
    <template #topbar-actions>
      <StatusTag :label="`角色：${roleLabel}`" tone="success" />
      <RouterLink class="button" to="/login">返回登录页</RouterLink>
    </template>
    <RouterView />
  </AppShell>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, RouterView } from "vue-router";
import AppShell from "../components/AppShell.vue";
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

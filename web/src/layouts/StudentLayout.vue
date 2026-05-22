<template>
  <AppShell
    brand-to="/student/dashboard"
    brand-mark="ISE"
    brand-text="学院综合服务平台"
    :nav-items="navItems"
    eyebrow="学生端 · self 数据范围"
    :title="`你好，${authStore.user?.realName || '同学'}`"
    role-value="authStore.roleCode"
    scope-value="self"
    home-api-value="/dashboard/student"
  >
    <template #topbar-actions>
      <StatusTag label="实名已绑定" tone="success" />
      <StatusTag :label="roleLabel" />
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
  { label: "首页", to: "/student/dashboard" },
  { label: "智能问答", to: "/student/kb" },
  { label: "党团流程", to: "/student/party" },
  { label: "通知中心", to: "/student/notices" },
  { label: "院内申请", to: "/student/applications" },
  { label: "个人画像", to: "/student/profile" },
  { label: "奖励荣誉", to: "/student/honors" },
]);

const roleLabel = computed(() => getLabel(authStore.roleCode, authStore.roleCode));
</script>

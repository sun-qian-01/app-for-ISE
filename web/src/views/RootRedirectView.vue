<template>
  <div class="shell shell--centered">
    <section class="panel auth-panel">
      <div class="section-head">
        <h2>正在跳转</h2>
      </div>
      <p class="hero__text">我们正在根据当前登录状态为你定位默认首页。</p>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace("/login");
    return;
  }
  if (authStore.roles.includes("teacher_admin") || authStore.roles.includes("college_leader") || authStore.roles.includes("system_admin")) {
    router.replace("/admin/dashboard");
    return;
  }
  router.replace("/student/dashboard");
});
</script>

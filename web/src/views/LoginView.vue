<template>
  <div class="shell shell--centered">
    <div class="auth-layout">
      <section class="hero hero--compact">
        <h1>学院学生综合服务与党团管理平台</h1>
        <p class="hero__text">
          使用学号或工号登录，进入对应角色工作台。
        </p>
      </section>

      <section class="panel auth-panel">
        <div class="section-head">
          <h2>账号登录</h2>
        </div>
        <form class="form" @submit.prevent="handleLogin">
          <label>
            <span>用户名</span>
            <input v-model="form.username" class="input" type="text" placeholder="学号或工号" />
          </label>
          <label>
            <span>密码</span>
            <input v-model="form.password" class="input" type="password" placeholder="请输入密码" />
          </label>
          <button class="button button--primary" type="submit">登录并进入工作台</button>
        </form>
        <p v-if="feedback" class="feedback">{{ feedback }}</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const feedback = ref("");
const form = reactive({
  username: "20220001",
  password: "123456",
});

async function handleLogin() {
  feedback.value = "";
  try {
    const result = await authStore.login(form);
    feedback.value = "登录成功，正在进入工作台。";

    const redirectPath = route.query.redirect;
    if (typeof redirectPath === "string" && redirectPath) {
      router.push(redirectPath);
      return;
    }

    if (result.user?.roles?.includes("teacher_admin") || result.user?.roles?.includes("system_admin")) {
      router.push("/admin/dashboard");
      return;
    }
    if (result.user?.roles?.includes("college_leader")) {
      router.push("/leader/dashboard");
      return;
    }
    router.push("/student/dashboard");
  } catch (error) {
    if (error?.code === "ERR_NETWORK") {
      feedback.value = "后端服务未连接（http://127.0.0.1:8080），请先启动后端再登录。";
      return;
    }
    feedback.value = error?.message || "登录失败，请检查用户名和密码";
  }
}
</script>

<template>
  <div class="shell shell--centered">
    <div class="auth-layout">
      <section class="hero hero--compact">
        <div class="hero__badge">/auth/login · /auth/me</div>
        <h1>学院学生综合服务与党团管理平台</h1>
        <p class="hero__text">
          当前为正式前端工程的首版登录页，已经接入路由、Pinia 和登录态恢复骨架。
        </p>
        <div class="tag-group">
          <span class="tag">student</span>
          <span class="tag">class_cadre</span>
          <span class="tag">teacher_admin</span>
          <span class="tag">college_leader</span>
        </div>
      </section>

      <section class="panel auth-panel">
        <div class="section-head">
          <h2>账号登录</h2>
          <span class="pill">POST /api/v1/auth/login</span>
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
          <label>
            <span>模拟角色</span>
            <select v-model="form.roleCode" class="input input--select">
              <option value="student">普通学生</option>
              <option value="class_cadre">班团骨干</option>
              <option value="teacher_admin">管理老师</option>
              <option value="college_leader">学院领导</option>
            </select>
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
  roleCode: "student",
});

async function handleLogin() {
  await authStore.login(form);
  feedback.value = "登录成功，正在进入工作台。";

  const redirectPath = route.query.redirect;
  if (typeof redirectPath === "string" && redirectPath) {
    router.push(redirectPath);
    return;
  }

  if (form.roleCode === "teacher_admin") {
    router.push("/admin/dashboard");
    return;
  }
  if (form.roleCode === "college_leader") {
    router.push("/leader/dashboard");
    return;
  }
  router.push("/student/dashboard");
}
</script>

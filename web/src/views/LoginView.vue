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
          <h2>{{ mode === "login" ? "账号登录" : "学生注册" }}</h2>
          <span class="pill">{{ mode === "login" ? "POST /api/v1/auth/login" : "POST /api/v1/auth/register" }}</span>
        </div>
        <div class="segmented">
          <button :class="{ 'is-active': mode === 'login' }" type="button" @click="switchMode('login')">登录</button>
          <button :class="{ 'is-active': mode === 'register' }" type="button" @click="switchMode('register')">学生注册</button>
        </div>
        <form v-if="mode === 'login'" class="form" @submit.prevent="handleLogin">
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
        <form v-else class="form" @submit.prevent="handleRegister">
          <label>
            <span>学号</span>
            <input
              v-model="registerForm.studentNo"
              class="input"
              type="text"
              inputmode="numeric"
              maxlength="10"
              pattern="\d{10}"
              placeholder="请输入10位数字学号"
              @input="normalizeStudentNo"
            />
          </label>
          <label>
            <span>姓名</span>
            <input v-model="registerForm.name" class="input" type="text" placeholder="请输入真实姓名" />
          </label>
          <div class="form-grid">
            <label>
              <span>年级</span>
              <input v-model="registerForm.grade" class="input" type="text" placeholder="例如 2026" />
            </label>
            <label>
              <span>专业</span>
              <input v-model="registerForm.major" class="input" type="text" placeholder="例如 软件工程" />
            </label>
          </div>
          <label>
            <span>班级</span>
            <input v-model="registerForm.className" class="input" type="text" placeholder="例如 软件工程1班" />
          </label>
          <label>
            <span>联系方式</span>
            <input v-model="registerForm.phone" class="input" type="tel" placeholder="请输入手机号或常用联系电话" />
          </label>
          <label>
            <span>邮箱</span>
            <input v-model="registerForm.email" class="input" type="email" placeholder="请输入常用邮箱" />
          </label>
          <label>
            <span>密码</span>
            <input v-model="registerForm.password" class="input" type="password" placeholder="请设置密码" />
          </label>
          <label>
            <span>重复密码</span>
            <input v-model="registerForm.confirmPassword" class="input" type="password" placeholder="请再次输入密码" />
          </label>
          <button class="button button--primary" type="submit">注册学生账号</button>
        </form>
        <p v-if="feedback" class="feedback">{{ feedback }}</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { registerStudentApi } from "../api/modules/authApi";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const mode = ref("login");
const feedback = ref("");
const form = reactive({
  username: "20220001",
  password: "123456",
  roleCode: "student",
});
const demoAccounts = {
  student: { username: "20220001", password: "123456" },
  class_cadre: { username: "20220018", password: "123456" },
  teacher_admin: { username: "teacher001", password: "123456" },
  college_leader: { username: "leader001", password: "123456" },
};
const registerForm = reactive({
  studentNo: "",
  name: "",
  grade: "",
  major: "",
  className: "",
  phone: "",
  email: "",
  politicalStatusLabel: "群众",
  password: "",
  confirmPassword: "",
});

function switchMode(nextMode) {
  mode.value = nextMode;
  feedback.value = "";
}

watch(
  () => form.roleCode,
  (roleCode) => {
    const account = demoAccounts[roleCode];
    if (!account) return;
    form.username = account.username;
    form.password = account.password;
    feedback.value = "";
  },
);

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
      router.push("/admin/dashboard");
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

async function handleRegister() {
  feedback.value = "";
  if (
    !registerForm.studentNo.trim() ||
    !registerForm.name.trim() ||
    !registerForm.grade.trim() ||
    !registerForm.major.trim() ||
    !registerForm.className.trim() ||
    !registerForm.password
  ) {
    feedback.value = "请填写学号、姓名、年级、专业、班级和密码。";
    return;
  }
  if (!/^\d{10}$/.test(registerForm.studentNo.trim())) {
    feedback.value = "学号必须是10位数字。";
    return;
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    feedback.value = "两次输入的密码不一致。";
    return;
  }

  try {
    await registerStudentApi(registerForm);
    form.username = registerForm.studentNo;
    form.password = registerForm.password;
    form.roleCode = "student";
    feedback.value = "注册成功，已填入登录信息。";
    registerForm.studentNo = "";
    registerForm.name = "";
    registerForm.grade = "";
    registerForm.major = "";
    registerForm.className = "";
    registerForm.phone = "";
    registerForm.email = "";
    registerForm.politicalStatusLabel = "群众";
    registerForm.password = "";
    registerForm.confirmPassword = "";
    mode.value = "login";
  } catch (error) {
    feedback.value = error.message || "注册失败，请稍后重试。";
  }
}

function normalizeStudentNo() {
  registerForm.studentNo = registerForm.studentNo.replace(/\D/g, "").slice(0, 10);
}
</script>

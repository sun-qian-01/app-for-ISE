<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader title="基础档案">
        <template #meta>
          <StatusTag tone="success" />
        </template>
      </PageHeader>
      <LoadingState v-if="loading" text="基础档案加载中..." />
      <ErrorState v-else-if="error" description="学生档案加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="info-list">
        <div><span>学号</span><strong>{{ data.studentNo }}</strong></div>
        <div><span>姓名</span><strong>{{ data.name }}</strong></div>
        <div><span>年级专业</span><strong>{{ data.grade }}级 {{ data.major }}</strong></div>
        <div><span>班级</span><strong>{{ data.className }}</strong></div>
        <div><span>政治面貌</span><strong>{{ data.politicalStatusLabel }}</strong></div>
        <div><span>联系方式</span><strong>{{ data.phoneMasked }}</strong></div>
      </div>
    </section>
    <section class="panel">
      <PageHeader title="成长记录" description="展示近期竞赛、实践与志愿服务记录。" />
      <LoadingState v-if="loading" text="成长记录加载中..." />
      <ErrorState v-else-if="error" description="成长记录加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <EmptyState v-if="!data.growthRecords.length" />
        <article v-for="item in data.growthRecords" :key="item.id" class="record">
          <div class="record__meta">{{ item.typeLabel }} · {{ item.date }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
        </article>
      </div>
    </section>
    <section class="panel">
      <PageHeader title="联系方式维护" />
      <form class="form" @submit.prevent="handleProfileSave">
        <label>
          <span>联系方式</span>
          <input v-model="profileForm.phone" class="input" type="tel" placeholder="请输入手机号或常用联系电话" />
        </label>
        <label>
          <span>邮箱</span>
          <input v-model="profileForm.email" class="input" type="email" placeholder="请输入常用邮箱" />
        </label>
        <label>
          <span>政治面貌</span>
          <select v-model="profileForm.politicalStatusLabel" class="input input--select">
            <option v-for="option in politicalStatusOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </label>
        <button class="button button--primary" type="submit">保存档案信息</button>
      </form>
      <p v-if="profileFeedback" class="feedback">{{ profileFeedback }}</p>
    </section>
    <section class="panel">
      <PageHeader title="修改密码" />
      <form class="form" @submit.prevent="handlePasswordChange">
        <label>
          <span>原密码</span>
          <input v-model="passwordForm.oldPassword" class="input" type="password" placeholder="请输入当前密码" />
        </label>
        <label>
          <span>新密码</span>
          <input v-model="passwordForm.newPassword" class="input" type="password" placeholder="请输入新密码" />
        </label>
        <label>
          <span>重复新密码</span>
          <input v-model="passwordForm.confirmPassword" class="input" type="password" placeholder="请再次输入新密码" />
        </label>
        <button class="button button--primary" type="submit">保存新密码</button>
      </form>
      <p v-if="passwordFeedback" class="feedback">{{ passwordFeedback }}</p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { changePasswordApi } from "../../api/modules/authApi";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { getMyGrowthRecords, getMyProfile, updateMyProfileApi } from "../../api/modules/studentApi";

const data = reactive({
  studentNo: "",
  name: "",
  grade: "",
  major: "",
  className: "",
  politicalStatusLabel: "",
  phone: "",
  phoneMasked: "",
  email: "",
  growthRecords: [],
});

const { loading, error, run } = useAsyncPage(async () => {
  const [profile, growthRecords] = await Promise.all([getMyProfile(), getMyGrowthRecords()]);
  return { profile, growthRecords };
});
const profileFeedback = ref("");
const politicalStatusOptions = ["群众", "共青团员", "入党申请人", "入党积极分子", "发展对象", "预备党员", "中共党员"];
const profileForm = reactive({
  phone: "",
  email: "",
  politicalStatusLabel: "群众",
});
const passwordFeedback = ref("");
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const { profile, growthRecords } = await run();
    applyBackendProfile(profile, growthRecords);
  } catch {}
}

function applyBackendProfile(profile, growthRecords) {
  data.studentNo = profile.student?.studentNo || "";
  data.name = profile.student?.name || "";
  data.grade = profile.student?.grade || "";
  data.major = profile.student?.major || "";
  data.className = profile.student?.className || "";
  data.politicalStatusLabel = profile.student?.politicalStatus || "群众";
  data.phone = profile.student?.phone || "";
  data.phoneMasked = profile.student?.phoneMasked || maskPhone(data.phone);
  data.email = profile.student?.email || "";
  data.growthRecords = (growthRecords || []).map((item) => ({
    id: item.id,
    typeLabel: recordTypeLabel(item.recordType),
    date: item.endDate ? `${item.startDate} 至 ${item.endDate}` : item.startDate,
    title: item.title,
    summary: item.description,
  }));
  profileForm.phone = data.phone || "";
  profileForm.email = data.email || "";
  profileForm.politicalStatusLabel = data.politicalStatusLabel || "群众";
}

function applyEditableProfile(profile) {
  Object.assign(data, profile);
  profileForm.phone = profile.phone || "";
  profileForm.email = profile.email || "";
  profileForm.politicalStatusLabel = profile.politicalStatusLabel || "群众";
}

function recordTypeLabel(type) {
  const map = {
    competition: "科研竞赛",
    practice: "社会实践",
    volunteer: "志愿服务",
    cadre: "干部任职",
    reward_punishment: "奖惩记录",
  };
  return map[type] || type;
}

function maskPhone(phone) {
  if (!phone) return "未填写";
  if (phone.length < 7) return phone;
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

async function handleProfileSave() {
  profileFeedback.value = "";
  if (!profileForm.phone.trim() || !profileForm.email.trim()) {
    profileFeedback.value = "请填写联系方式和邮箱。";
    return;
  }

  try {
    const profile = await updateMyProfileApi(profileForm);
    applyEditableProfile(profile);
    profileFeedback.value = "个人画像信息已保存。";
  } catch (error) {
    profileFeedback.value = error.message || "保存失败，请稍后重试。";
  }
}

async function handlePasswordChange() {
  passwordFeedback.value = "";
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    passwordFeedback.value = "请完整填写原密码和新密码。";
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordFeedback.value = "两次输入的新密码不一致。";
    return;
  }
  if (passwordForm.oldPassword === passwordForm.newPassword) {
    passwordFeedback.value = "新密码不能与原密码相同。";
    return;
  }

  try {
    await changePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
    passwordFeedback.value = "密码已修改，下次登录请使用新密码。";
  } catch (error) {
    passwordFeedback.value = error.message || "密码修改失败，请稍后重试。";
  }
}
</script>

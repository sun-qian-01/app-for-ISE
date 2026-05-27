<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader title="基础档案">
        <template #meta>
          <StatusTag label="敏感字段脱敏" tone="success" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { getMyGrowthRecords, getMyProfile } from "../../api/modules/studentApi";

const data = reactive({
  studentNo: "",
  name: "",
  grade: "",
  major: "",
  className: "",
  politicalStatusLabel: "",
  phoneMasked: "",
  growthRecords: [],
});

const { loading, error, run } = useAsyncPage(async () => {
  const [profile, growthRecords] = await Promise.all([getMyProfile(), getMyGrowthRecords()]);
  return { profile, growthRecords };
});

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const { profile, growthRecords } = await run();
    data.studentNo = profile.student?.studentNo || "";
    data.name = profile.student?.name || "";
    data.grade = profile.student?.grade || "";
    data.major = profile.student?.major || "";
    data.className = profile.student?.className || "";
    data.politicalStatusLabel = profile.student?.politicalStatus || "";
    data.phoneMasked = profile.student?.phoneMasked || "";
    data.growthRecords = (growthRecords || []).map((item) => ({
      id: item.id,
      typeLabel: recordTypeLabel(item.recordType),
      date: item.endDate ? `${item.startDate} 至 ${item.endDate}` : item.startDate,
      title: item.title,
      summary: item.description,
    }));
  } catch {}
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
</script>

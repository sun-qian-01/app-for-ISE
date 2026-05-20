<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader title="基础档案">
        <template #meta>
          <StatusTag label="敏感字段脱敏" tone="success" />
        </template>
      </PageHeader>
      <div class="info-list">
        <div><span>学号</span><strong>{{ data.studentNo }}</strong></div>
        <div><span>姓名</span><strong>{{ data.name }}</strong></div>
        <div><span>年级专业</span><strong>{{ data.grade }}级 {{ data.major }}</strong></div>
        <div><span>班级</span><strong>{{ data.className }}</strong></div>
        <div><span>政治面貌</span><strong>{{ data.politicalStatusLabel }}</strong></div>
        <div><span>联系方式</span><strong>{{ data.phoneMasked }}</strong></div>
        <div><span>邮箱</span><strong>{{ data.email }}</strong></div>
      </div>
    </section>
    <section class="panel">
      <PageHeader title="成长记录" api="GET /students/{id}/growth-records" />
      <div class="stack">
        <EmptyState v-if="!data.growthRecords.length" />
        <article v-for="item in data.growthRecords" :key="item.title" class="record">
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
import PageHeader from "../../components/common/PageHeader.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { fetchProfile } from "../../mocks/server";

const data = reactive({
  studentNo: "",
  name: "",
  grade: "",
  major: "",
  className: "",
  politicalStatusLabel: "",
  phoneMasked: "",
  email: "",
  growthRecords: [],
});

onMounted(async () => {
  Object.assign(data, await fetchProfile());
});
</script>

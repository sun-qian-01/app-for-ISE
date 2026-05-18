<template>
  <section class="panel">
    <div class="section-head">
      <h2>学生画像管理</h2>
      <span class="pill">GET /students</span>
    </div>
    <div class="table-like">
      <div class="table-row table-head">
        <span>学号</span>
        <span>姓名</span>
        <span>班级</span>
        <span>状态</span>
        <span>标签</span>
      </div>
      <div v-for="item in items" :key="item.studentNo" class="table-row">
        <span>{{ item.studentNo }}</span>
        <strong>{{ item.name }}</strong>
        <span>{{ item.className }}</span>
        <span>{{ item.statusText }}</span>
        <span>{{ item.tags.join("、") }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchAdminStudents } from "../../mocks/server";

const items = ref([]);

onMounted(async () => {
  items.value = await fetchAdminStudents();
});
</script>

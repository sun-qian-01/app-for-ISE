<template>
  <section class="panel">
    <div class="section-head">
      <h2>奖励荣誉</h2>
      <span class="subtle-note">展示学院公开荣誉与个人成长亮点</span>
    </div>
    <div class="grid grid--three">
      <article v-for="item in items" :key="item.id" class="card honor-card">
        <div class="card__meta">{{ item.year }} · {{ item.categoryLabel }}</div>
        <h3>{{ item.title }}</h3>
        <p><strong>{{ item.owner }}</strong></p>
        <p>{{ item.story }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getPublicHonors } from "../../api/modules/honorApi";

const items = ref([]);

onMounted(async () => {
  items.value = await getPublicHonors();
});
</script>

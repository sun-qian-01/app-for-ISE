<template>
  <section class="panel">
    <div class="section-head">
      <h2>荣誉管理</h2>
      <span class="pill">GET /honors</span>
    </div>
    <div class="grid grid--three">
      <article v-for="item in items" :key="item.title" class="card honor-card">
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
import { fetchHonors } from "../../mocks/server";

const items = ref([]);

onMounted(async () => {
  items.value = await fetchHonors();
});
</script>

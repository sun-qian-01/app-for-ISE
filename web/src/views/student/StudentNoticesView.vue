<template>
  <section class="panel">
    <div class="section-head">
      <h2>通知中心</h2>
      <span class="pill">GET /notices/my</span>
    </div>
    <div class="stack">
      <article v-for="item in items" :key="item.title" class="notice-card" :class="{ 'is-unread': !item.read }">
        <div class="notice-card__meta">{{ item.date }} · {{ item.audience }}</div>
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
        <div class="tag-group">
          <span v-for="channel in item.channelLabels" :key="channel" class="tag">{{ channel }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchNotices } from "../../mocks/server";

const items = ref([]);

onMounted(async () => {
  items.value = await fetchNotices();
});
</script>

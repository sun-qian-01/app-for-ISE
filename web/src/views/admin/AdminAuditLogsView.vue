<template>
  <section class="panel">
    <PageHeader title="审计日志" api="GET /audit-logs" />
    <SearchBar>
      <select v-model="moduleFilter" class="input input--select">
        <option value="all">全部模块</option>
        <option value="学生画像">学生画像</option>
        <option value="通知">通知</option>
      </select>
      <select v-model="resultFilter" class="input input--select">
        <option value="all">全部结果</option>
        <option value="成功">成功</option>
        <option value="失败">失败</option>
      </select>
    </SearchBar>
    <div class="stack">
      <EmptyState v-if="!filteredItems.length" />
      <RecordCard
        v-for="item in filteredItems"
        :key="`${item.actor}-${item.time}`"
        :meta="`${item.time} · ${item.module}`"
        :title="item.actor"
        :description="item.action"
        :tone="item.result === '成功' ? 'success' : 'warn'"
      >
        <template #tags>
          <StatusTag :label="item.result" :tone="item.result === '成功' ? 'success' : 'warn'" />
        </template>
      </RecordCard>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { fetchAuditLogs } from "../../mocks/server";

const items = ref([]);
const moduleFilter = ref("all");
const resultFilter = ref("all");

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchModule = moduleFilter.value === "all" || item.module === moduleFilter.value;
    const matchResult = resultFilter.value === "all" || item.result === resultFilter.value;
    return matchModule && matchResult;
  }),
);

onMounted(async () => {
  items.value = await fetchAuditLogs();
});
</script>

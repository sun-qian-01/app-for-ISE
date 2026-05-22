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
    <LoadingState v-if="loading" text="审计日志加载中..." />
    <ErrorState v-else-if="error" description="审计日志加载失败，请稍后重试。" @retry="loadData" />
    <div v-else class="stack">
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
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { getAuditLogs } from "../../api/modules/auditApi";

const items = ref([]);
const moduleFilter = ref("all");
const resultFilter = ref("all");
const { loading, error, run } = useAsyncPage(getAuditLogs);

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchModule = moduleFilter.value === "all" || item.module === moduleFilter.value;
    const matchResult = resultFilter.value === "all" || item.result === resultFilter.value;
    return matchModule && matchResult;
  }),
);

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    items.value = await run();
  } catch {}
}
</script>

<template>
  <section class="panel">
    <PageHeader title="系统日志" api="GET /system-logs">
      <template #actions>
        <StatusTag v-if="canViewStack" label="具备查看堆栈权限" tone="success" />
        <StatusTag v-else label="当前仅展示摘要日志" tone="warn" />
      </template>
    </PageHeader>
    <SearchBar>
      <select v-model="levelFilter" class="input input--select">
        <option value="all">全部级别</option>
        <option value="错误">错误</option>
        <option value="警告">警告</option>
        <option value="信息">信息</option>
      </select>
      <input v-model="requestIdKeyword" class="input" type="search" placeholder="筛选 requestId" />
    </SearchBar>
    <div class="stack">
      <EmptyState v-if="!filteredItems.length" title="暂无系统事件" description="当前没有需要展示的系统日志。" />
      <RecordCard
        v-for="item in filteredItems"
        :key="item.requestId"
        :meta="`${item.levelLabel} · ${item.module} · ${item.requestId}`"
        :title="item.message"
        :description="item.path"
        :tone="item.levelLabel === '错误' ? 'current' : item.levelLabel === '警告' ? 'warn' : 'success'"
      >
        <template #extra>
          {{ item.detail }}
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
import { usePermission } from "../../composables/usePermission";
import { fetchSystemLogs } from "../../mocks/server";

const items = ref([]);
const levelFilter = ref("all");
const requestIdKeyword = ref("");
const { hasPermission, hasRole } = usePermission();

const canViewStack = computed(() => hasPermission("system-log:stack:view") || hasRole("system_admin"));

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchLevel = levelFilter.value === "all" || item.levelLabel === levelFilter.value;
    const matchRequestId = !requestIdKeyword.value || item.requestId.includes(requestIdKeyword.value);
    return matchLevel && matchRequestId;
  }),
);

onMounted(async () => {
  items.value = await fetchSystemLogs();
});
</script>

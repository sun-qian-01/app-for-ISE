<template>
  <section class="panel">
    <PageHeader title="学生画像管理" api="GET /students">
      <template #actions>
        <StatusTag v-if="canViewSensitive" label="具备敏感字段查看权限" tone="success" />
        <StatusTag v-else label="当前仅展示摘要信息" tone="warn" />
      </template>
    </PageHeader>
    <SearchBar>
      <input v-model="keyword" class="input" type="search" placeholder="搜索学号、姓名、班级" />
      <select v-model="statusFilter" class="input input--select">
        <option value="all">全部状态</option>
        <option value="在读">在读</option>
        <option value="毕业年级">毕业年级</option>
        <option value="重点关注">重点关注</option>
      </select>
    </SearchBar>
    <LoadingState v-if="loading" text="学生列表加载中..." />
    <ErrorState v-else-if="error" description="学生列表加载失败，请稍后重试。" @retry="loadData" />
    <DataTable
      v-else
      :columns="columns"
      :rows="filteredItems"
      row-key="studentNo"
      template-columns="1fr 0.8fr 1.4fr 0.8fr 1.6fr"
      empty-title="没有匹配学生"
      empty-description="请调整搜索词或筛选条件。"
    >
      <template #cell-name="{ row }">
        <strong>{{ row.name }}</strong>
      </template>
      <template #cell-tags="{ row }">
        <span>{{ row.tags.join("、") }}</span>
      </template>
    </DataTable>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import DataTable from "../../components/common/DataTable.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { usePermission } from "../../composables/usePermission";
import { getStudentList } from "../../api/modules/studentApi";

const items = ref([]);
const keyword = ref("");
const statusFilter = ref("all");
const { hasPermission } = usePermission();
const { loading, error, run } = useAsyncPage(getStudentList);

const columns = [
  { key: "studentNo", label: "学号" },
  { key: "name", label: "姓名" },
  { key: "className", label: "班级" },
  { key: "statusText", label: "状态" },
  { key: "tags", label: "标签" },
];

const canViewSensitive = computed(() => hasPermission("student:sensitive:view"));

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchKeyword = !keyword.value || `${item.studentNo} ${item.name} ${item.className}`.includes(keyword.value);
    const matchStatus = statusFilter.value === "all" || item.statusText === statusFilter.value;
    return matchKeyword && matchStatus;
  }),
);

onMounted(async () => {
  loadData();
});

async function loadData() {
  try {
    items.value = await run();
  } catch {}
}
</script>

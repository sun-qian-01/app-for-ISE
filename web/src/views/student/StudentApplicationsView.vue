<template>
  <section class="panel">
    <PageHeader
      title="院内申请"
      description="查看我的申请状态、附件数量和已生成文件。"
    />

    <SearchBar>
      <select v-model="statusFilter" class="input input--select">
        <option value="all">全部状态</option>
        <option value="审核中">审核中</option>
        <option value="已通过">已通过</option>
        <option value="已驳回">已驳回</option>
      </select>
      <input v-model="keyword" class="input" type="search" placeholder="搜索申请编号、类型、用途" />
    </SearchBar>

    <LoadingState v-if="loading" text="申请记录加载中..." />
    <ErrorState v-else-if="error" description="申请记录加载失败，请稍后重试。" @retry="loadData" />
    <div v-else class="stack">
      <EmptyState
        v-if="!filteredItems.length"
        title="暂无匹配申请"
        description="可以切换状态或关键字筛选，也可以后续补充新申请入口。"
      />
      <RecordCard
        v-for="item in filteredItems"
        :key="item.no"
        :meta="`${item.no} · ${item.createdAt}`"
        :title="item.typeLabel"
        :description="`用途：${item.purpose || '-'}`"
      >
        <template #tags>
          <StatusTag :label="item.statusLabel" :tone="getStatusTone(item.statusLabel)" />
          <span class="tag">{{ item.approver || '-' }}</span>
        </template>
        <template #extra>
          <div>申请人：{{ item.applicant }}</div>
          <div>申请标题：{{ item.title }}</div>
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
import { getMyApplications } from "../../api/modules/applicationApi";
import { useAuthStore } from "../../stores/auth";

const authStore = useAuthStore();
const items = ref([]);
const keyword = ref("");
const statusFilter = ref("all");
const { loading, error, run } = useAsyncPage(() => getMyApplications({ pageNo: 1, pageSize: 50 }));

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchStatus = statusFilter.value === "all" || item.statusLabel === statusFilter.value;
    const text = `${item.no} ${item.typeLabel} ${item.purpose || ""}`.toLowerCase();
    const matchKeyword = !keyword.value || text.includes(keyword.value.toLowerCase());
    return matchStatus && matchKeyword;
  }),
);

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const page = await run();
    items.value = (page.records || []).map((item) => ({
      id: item.id,
      no: item.applicationNo,
      typeLabel: applicationTypeLabel(item.applicationType),
      title: item.title,
      statusLabel: statusLabel(item.status),
      approver: item.currentApprover,
      applicant: authStore.user?.realName || "本人",
      purpose: item.purpose,
      createdAt: item.submittedAt,
    }));
  } catch {}
}

function applicationTypeLabel(type) {
  if (type === "certificate") return "证明申请";
  return type || "申请";
}

function statusLabel(status) {
  if (status === "approved") return "已通过";
  if (status === "rejected") return "已驳回";
  if (status === "revoked") return "已撤回";
  if (status === "submitted" || status === "reviewing") return "审核中";
  return status || "-";
}

function getStatusTone(status) {
  if (status === "已通过") return "success";
  if (status === "已驳回") return "warn";
  return "default";
}
</script>

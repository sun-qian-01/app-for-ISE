<template>
  <section class="panel">
    <PageHeader
      title="党团流程管理"
      api="GET /party/todos"
      description="展示待审核阶段，并支持通过/驳回处理。"
    />

    <SearchBar>
      <select v-model="statusFilter" class="input input--select">
        <option value="all">全部状态</option>
        <option value="submitted">已提交</option>
        <option value="reviewing">审核中</option>
        <option value="returned">已退回</option>
      </select>
      <input v-model="keyword" class="input" type="search" placeholder="搜索学号、姓名、班级、阶段" />
    </SearchBar>

    <LoadingState v-if="loading" text="党团待办加载中..." />
    <ErrorState v-else-if="error" description="党团待办加载失败，请稍后重试。" @retry="loadData" />
    <div v-else class="stack">
      <EmptyState
        v-if="!filteredItems.length"
        title="当前无待办"
        description="暂无需要审核的党团阶段记录。"
      />
      <RecordCard
        v-for="item in filteredItems"
        :key="item.stageRecordId"
        :meta="`${item.studentNo} · ${item.className} · 截止 ${item.dueAt}`"
        :title="`${item.studentName} - ${item.stageName}`"
        :description="`材料 ${item.materialCount} 份，待审核 ${item.pendingMaterialCount} 份`"
      >
        <template #tags>
          <StatusTag :label="statusLabel(item.stageStatus)" :tone="statusTone(item.stageStatus)" />
        </template>
        <template #actions>
          <button class="button button--primary" type="button" @click="approve(item.stageRecordId)">通过</button>
          <button class="button" type="button" @click="reject(item.stageRecordId)">驳回</button>
        </template>
      </RecordCard>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { getPartyTodos, reviewPartyStage } from "../../api/modules/partyApi";

const items = ref([]);
const loading = ref(false);
const error = ref(false);
const statusFilter = ref("all");
const keyword = ref("");
const filteredItems = computed(() =>
  (items.value || []).filter((item) => {
    const text = `${item.studentNo} ${item.studentName} ${item.className} ${item.stageName}`.toLowerCase();
    return !keyword.value || text.includes(keyword.value.toLowerCase());
  }),
);

onMounted(() => {
  loadData();
});

watch(statusFilter, () => {
  loadData();
});

async function loadData() {
  loading.value = true;
  error.value = false;
  try {
    items.value = await getPartyTodos({ status: statusFilter.value });
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

async function approve(stageRecordId) {
  await reviewPartyStage(stageRecordId, "approve", "材料完整，阶段通过");
  await loadData();
}

async function reject(stageRecordId) {
  await reviewPartyStage(stageRecordId, "reject", "材料需补充，请按清单补正后重提");
  await loadData();
}

function statusLabel(status) {
  if (status === "submitted") return "已提交";
  if (status === "reviewing") return "审核中";
  if (status === "returned") return "已退回";
  if (status === "approved") return "已通过";
  return status;
}

function statusTone(status) {
  if (status === "approved") return "success";
  if (status === "returned") return "warn";
  return "default";
}
</script>

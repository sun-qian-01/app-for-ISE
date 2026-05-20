<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="审批处理"
        api="GET /applications/approvals/pending"
        description="查看审批队列、申请人摘要、附件情况和生成文件状态。"
      />

      <SearchBar>
        <select v-model="statusFilter" class="input input--select">
          <option value="all">全部状态</option>
          <option value="审核中">审核中</option>
          <option value="已通过">已通过</option>
          <option value="已驳回">已驳回</option>
        </select>
        <input v-model="keyword" class="input" type="search" placeholder="搜索申请编号、申请人、类型" />
      </SearchBar>

      <LoadingState v-if="loading" text="审批列表加载中..." />
      <ErrorState v-else-if="error" description="审批列表加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <EmptyState
          v-if="!filteredItems.length"
          title="暂无匹配审批"
          description="可以切换状态或关键字查看不同审批记录。"
        />
        <RecordCard
          v-for="item in filteredItems"
          :key="item.no"
          :meta="`${item.no} · ${item.createdAt}`"
          :title="item.typeLabel"
          :description="`用途：${item.purpose}`"
        >
          <template #tags>
            <StatusTag :label="item.statusLabel" :tone="getStatusTone(item.statusLabel)" />
            <span class="tag">申请人：{{ item.applicant }}</span>
            <span class="tag">附件 {{ item.attachmentCount }} 份</span>
          </template>
          <template #actions>
            <button class="button button--primary" type="button">通过</button>
            <button class="button" type="button">驳回</button>
          </template>
          <template #extra>
            <div>当前审批人：{{ item.approver }}</div>
            <div>生成文件：{{ item.generatedFileName || "尚未生成" }}</div>
          </template>
        </RecordCard>
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="审批规则"
        description="保留审核意见、驳回原因和模板联动的前端说明，便于后续继续补单据详情。"
      />
      <div class="stack">
        <RecordCard
          meta="审批动作"
          title="通过 / 驳回必须带审核意见"
          description="后续正式接入接口时，建议表单字段统一对齐 approvalComment、approvalResult。"
        />
        <RecordCard
          meta="文件联动"
          title="通过后自动生成证明文件"
          description="申请详情页和审批页应复用 generatedFileName 展示逻辑，并预留下载按钮。"
        />
        <RecordCard
          meta="附件校验"
          title="驳回时需指出缺失或错误附件"
          description="当前先以说明面板承载规则，后续可以扩展为审批抽屉和附件预览区。"
        />
      </div>
    </section>
  </div>
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
import { getPendingApplications } from "../../api/modules/applicationApi";

const items = ref([]);
const loading = ref(false);
const error = ref(false);
const keyword = ref("");
const statusFilter = ref("all");

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchStatus = statusFilter.value === "all" || item.statusLabel === statusFilter.value;
    const text = `${item.no} ${item.applicant} ${item.typeLabel}`.toLowerCase();
    const matchKeyword = !keyword.value || text.includes(keyword.value.toLowerCase());
    return matchStatus && matchKeyword;
  }),
);

onMounted(() => {
  loadData();
});

async function loadData() {
  loading.value = true;
  error.value = false;
  try {
    items.value = await getPendingApplications();
  } catch (err) {
    console.error(err);
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function getStatusTone(status) {
  if (status === "已通过") return "success";
  if (status === "已驳回") return "warn";
  return "default";
}
</script>

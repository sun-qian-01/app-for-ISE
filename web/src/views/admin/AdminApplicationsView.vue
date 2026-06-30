<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="审批处理"
        description="查看审批队列、申请人摘要、附件情况和生成文件状态。"
      />

      <SearchBar>
        <select v-model="statusFilter" class="input input--select">
          <option value="all">全部状态</option>
          <option value="审核中">审核中</option>
          <option value="已通过">已通过</option>
          <option value="已驳回">已驳回</option>
        </select>
        <input v-model="keyword" class="input" type="search" placeholder="搜索申请编号、申请标题、类型" />
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
          :key="item.id"
          :meta="`${item.no} · ${item.createdAt}`"
          :title="item.typeLabel"
          :description="`用途：${item.purpose || '-'}`"
        >
          <template #tags>
            <StatusTag :label="item.statusLabel" :tone="getStatusTone(item.statusLabel)" />
            <span class="tag">申请标题：{{ item.title }}</span>
            <span v-if="item.attachment" class="tag">附件：{{ item.attachment.fileName }}</span>
          </template>
          <template #actions>
            <button class="button" type="button" @click="viewDetail(item.id)">查看详情</button>
            <button v-if="item.attachment" class="button" type="button" @click="downloadAttachment(item.attachment)">
              下载附件
            </button>
            <button class="button button--primary" type="button" @click="approve(item.id)">通过</button>
            <button class="button" type="button" @click="reject(item.id)">驳回</button>
          </template>
          <template #extra>
            <div>当前审批人：{{ item.approver || '-' }}</div>
            <div>附件：{{ item.attachment?.fileName || '无' }}</div>
          </template>
        </RecordCard>
        <PaginationBar
          v-if="applicationTotal > pageSize"
          :page-no="pageNo"
          :page-size="pageSize"
          :total="applicationTotal"
          @change="changePage"
        />
      </div>
    </section>

    <section class="panel">
      <PageHeader
        :title="selectedDetail ? '申请详情' : '审批规则'"
        :description="selectedDetail ? '查看申请表单、附件和审批流转记录。' : '审批动作会写入记录，并更新申请状态。'"
      />
      <LoadingState v-if="detailLoading" text="申请详情加载中..." />
      <div v-else-if="selectedDetail" class="stack">
        <RecordCard
          :meta="`${selectedDetail.applicationNo} · ${selectedDetail.submittedAt}`"
          :title="selectedDetail.title"
          :description="`用途：${selectedDetail.purpose || '-'}`"
        >
          <template #tags>
            <StatusTag :label="statusLabel(selectedDetail.status)" :tone="getStatusTone(statusLabel(selectedDetail.status))" />
            <span class="tag">{{ applicationTypeLabel(selectedDetail.applicationType) }}</span>
          </template>
          <template #actions>
            <button
              v-if="selectedDetail.attachment"
              class="button button--primary"
              type="button"
              @click="downloadAttachment(selectedDetail.attachment)"
            >
              下载附件
            </button>
            <button class="button" type="button" @click="selectedDetail = null">关闭详情</button>
          </template>
          <template #extra>
            <div>当前审批人：{{ selectedDetail.currentApprover || '-' }}</div>
            <div>附件：{{ selectedDetail.attachment?.fileName || '无' }}</div>
            <div>补充说明：{{ selectedDetail.formData?.description || '-' }}</div>
          </template>
        </RecordCard>
        <RecordCard
          v-for="record in selectedDetail.approvalRecords || []"
          :key="`${record.action}-${record.operatedAt}`"
          :meta="record.operatedAt"
          :title="record.nodeName"
          :description="record.opinion || '-'"
        >
          <template #extra>
            操作人：{{ record.operator || '-' }}，动作：{{ record.action || '-' }}
          </template>
        </RecordCard>
      </div>
      <div class="stack">
        <RecordCard
          v-if="!selectedDetail && !detailLoading"
          meta="审批动作"
          title="通过 / 驳回必须带审核意见"
          description="当前演示动作使用固定审核意见，后续可扩展为输入框。"
        />
        <RecordCard
          v-if="!selectedDetail && !detailLoading"
          meta="状态控制"
          title="仅 submitted/reviewing 状态可审批"
          description="后端会校验状态流转，重复审批会返回状态冲突。"
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
import PaginationBar from "../../components/common/PaginationBar.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { approveApplication, getApplicationDetail, getPendingApplications, rejectApplication } from "../../api/modules/applicationApi";
import { downloadWithAuth } from "../../utils/downloadFile";

const items = ref([]);
const loading = ref(false);
const error = ref(false);
const keyword = ref("");
const statusFilter = ref("all");
const applicationTotal = ref(0);
const pageNo = ref(1);
const pageSize = 20;
const selectedDetail = ref(null);
const detailLoading = ref(false);

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchStatus = statusFilter.value === "all" || item.statusLabel === statusFilter.value;
    const text = `${item.no} ${item.title} ${item.typeLabel}`.toLowerCase();
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
    const page = await getPendingApplications({ pageNo: pageNo.value, pageSize });
    applicationTotal.value = Number(page.total) || 0;
    items.value = (page.records || []).map((item) => ({
      id: item.id,
      no: item.applicationNo,
      typeLabel: applicationTypeLabel(item.applicationType),
      title: item.title,
      statusLabel: statusLabel(item.status),
      approver: item.currentApprover,
      purpose: item.purpose,
      createdAt: item.submittedAt,
      attachment: item.attachment || null,
    }));
  } catch (err) {
    console.error(err);
    error.value = true;
  } finally {
    loading.value = false;
  }
}

async function viewDetail(applicationId) {
  detailLoading.value = true;
  try {
    selectedDetail.value = await getApplicationDetail(applicationId);
  } catch (err) {
    console.error(err);
  } finally {
    detailLoading.value = false;
  }
}

async function downloadAttachment(attachment) {
  if (!attachment?.fileUrl) {
    return;
  }
  try {
    await downloadWithAuth(attachment.fileUrl, attachment.fileName || "application-attachment");
  } catch (err) {
    window.alert(err?.message || "附件下载失败，请稍后重试。");
  }
}

async function changePage(nextPageNo) {
  pageNo.value = nextPageNo;
  await loadData();
}

async function approve(applicationId) {
  try {
    await approveApplication(applicationId, "材料齐全，同意通过");
    await loadData();
  } catch (err) {
    console.error(err);
  }
}

async function reject(applicationId) {
  try {
    await rejectApplication(applicationId, "材料不完整，请补充后重新提交");
    await loadData();
  } catch (err) {
    console.error(err);
  }
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

<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="提交院内申请"
        description="填写申请类型、用途和补充说明，可上传附件，提交后进入老师审批流程。"
        api="POST /applications"
      />

      <form class="form" @submit.prevent="handleCreate">
        <label>
          <span>申请类型</span>
          <select v-model="createForm.applicationType" class="input input--select">
            <option value="certificate">证明申请</option>
            <option value="leave">请假申请</option>
            <option value="seal">盖章申请</option>
          </select>
        </label>
        <label>
          <span>申请标题</span>
          <input v-model="createForm.title" class="input" type="text" placeholder="例如 在读证明申请" />
        </label>
        <label>
          <span>申请用途</span>
          <input v-model="createForm.purpose" class="input" type="text" placeholder="例如 实习单位材料提交" />
        </label>
        <label>
          <span>补充说明</span>
          <textarea v-model="createForm.description" class="input textarea" rows="4" placeholder="填写接收单位、使用场景、时间要求等补充信息"></textarea>
        </label>
        <label>
          <span>附件</span>
          <input class="input" type="file" @change="handleFileChange" />
        </label>
        <button class="button button--primary" type="submit" :disabled="submitting">
          {{ submitting ? "提交中..." : "提交申请" }}
        </button>
      </form>
      <p v-if="formFeedback" class="feedback">{{ formFeedback }}</p>
    </section>

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
          description="可以切换状态或关键字筛选。"
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
        <PaginationBar
          v-if="applicationTotal > pageSize"
          :page-no="pageNo"
          :page-size="pageSize"
          :total="applicationTotal"
          @change="changePage"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import PaginationBar from "../../components/common/PaginationBar.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { createApplication, getMyApplications } from "../../api/modules/applicationApi";
import { uploadFile } from "../../api/modules/fileApi";
import { useAuthStore } from "../../stores/auth";

const authStore = useAuthStore();
const items = ref([]);
const keyword = ref("");
const statusFilter = ref("all");
const applicationTotal = ref(0);
const pageNo = ref(1);
const pageSize = 20;
const submitting = ref(false);
const formFeedback = ref("");
const pendingFile = ref(null);
const createForm = reactive({
  applicationType: "certificate",
  title: "",
  purpose: "",
  description: "",
});
const { loading, error, run } = useAsyncPage(() => getMyApplications({ pageNo: pageNo.value, pageSize }));

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
    applicationTotal.value = Number(page.total) || 0;
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

async function changePage(nextPageNo) {
  pageNo.value = nextPageNo;
  await loadData();
}

async function handleCreate() {
  formFeedback.value = "";
  if (!createForm.title.trim()) {
    formFeedback.value = "请填写申请标题。";
    return;
  }
  if (!createForm.purpose.trim()) {
    formFeedback.value = "请填写申请用途。";
    return;
  }

  submitting.value = true;
  try {
    let attachment = null;
    if (pendingFile.value) {
      attachment = await uploadFile(pendingFile.value, "application_attachment");
    }

    const result = await createApplication({
      applicationType: createForm.applicationType,
      templateId: templateIdForType(createForm.applicationType),
      title: createForm.title,
      purpose: createForm.purpose,
      formData: {
        description: createForm.description,
        attachmentFileId: attachment?.fileId || null,
        attachmentFileName: attachment?.fileName || "",
      },
    });
    formFeedback.value = `申请已提交，编号：${result.applicationNo || "待生成"}`;
    pageNo.value = 1;
    createForm.title = "";
    createForm.purpose = "";
    createForm.description = "";
    pendingFile.value = null;
    await loadData();
  } catch (error) {
    formFeedback.value = error?.message || "申请提交失败，请稍后重试。";
  } finally {
    submitting.value = false;
  }
}

function handleFileChange(event) {
  pendingFile.value = event.target.files?.[0] || null;
}

function templateIdForType(type) {
  if (type === "leave") return 2;
  if (type === "seal") return 3;
  return 1;
}

function applicationTypeLabel(type) {
  if (type === "certificate") return "证明申请";
  if (type === "leave") return "请假申请";
  if (type === "seal") return "盖章申请";
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

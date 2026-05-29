<template>
  <section class="panel">
    <PageHeader
      title="院内申请"
      description="提交新申请并查看我的申请状态。"
    />

    <div class="import-box">
      <div>
        <strong>提交新申请</strong>
        <p class="subtle-note">支持填写用途并上传附件，提交后进入审核流程。</p>
      </div>
      <form class="import-box__actions" @submit.prevent="submitApplication">
        <select v-model.number="createForm.templateId" class="input input--select">
          <option :value="1">在读证明申请</option>
          <option :value="2">成绩证明申请</option>
          <option :value="3">党团材料盖章申请</option>
        </select>
        <input v-model.trim="createForm.title" class="input" type="text" placeholder="申请标题" />
        <input v-model.trim="createForm.purpose" class="input" type="text" placeholder="申请用途（可选）" />
        <input class="input" type="file" @change="handleFileChange" />
        <button class="button button--primary" type="submit" :disabled="creating">
          {{ creating ? "提交中..." : "提交申请" }}
        </button>
      </form>
    </div>
    <p v-if="createFeedback" class="feedback">{{ createFeedback }}</p>

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
import { computed, onMounted, reactive, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
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
const creating = ref(false);
const createFeedback = ref("");
const pendingFile = ref(null);
const createForm = reactive({
  templateId: 1,
  title: "在读证明申请",
  purpose: "",
});
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

function handleFileChange(event) {
  pendingFile.value = event.target.files?.[0] || null;
}

async function submitApplication() {
  createFeedback.value = "";
  if (!createForm.title) {
    createFeedback.value = "请先填写申请标题。";
    return;
  }

  creating.value = true;
  try {
    let attachment = null;
    if (pendingFile.value) {
      attachment = await uploadFile(pendingFile.value, "application_attachment");
    }

    const payload = {
      applicationType: "certificate",
      templateId: createForm.templateId,
      title: createForm.title,
      purpose: createForm.purpose,
      formData: {
        attachmentFileId: attachment?.fileId || null,
        attachmentFileName: attachment?.fileName || "",
      },
    };
    const result = await createApplication(payload);
    createFeedback.value = `提交成功，申请编号：${result.applicationNo}`;
    pendingFile.value = null;
    createForm.purpose = "";
    await loadData();
  } catch (error) {
    createFeedback.value = error?.message || "提交失败，请稍后重试。";
  } finally {
    creating.value = false;
  }
}
</script>

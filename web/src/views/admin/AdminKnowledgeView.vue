<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="知识库管理"
        description="管理知识文章的分类、版本、发布状态和来源文件。"
      />

      <SearchBar>
        <select v-model="categoryFilter" class="input input--select">
          <option value="all">全部分类</option>
          <option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="keyword" class="input" type="search" placeholder="搜索文章标题、摘要、来源" />
      </SearchBar>

      <LoadingState v-if="loading" text="知识条目加载中..." />
      <ErrorState v-else-if="error" description="知识条目加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <EmptyState
          v-if="!filteredArticles.length"
          title="暂无匹配条目"
          description="可以调整分类与关键字，后续可扩展新建文章能力。"
        />
        <RecordCard
          v-for="item in filteredArticles"
          :key="item.articleId"
          :to="{ name: 'admin-kb-article', params: { articleId: item.articleId } }"
          :meta="`${item.categoryLabel} · ${item.version}`"
          :title="item.title"
          :description="item.summary"
        >
          <template #tags>
            <StatusTag :label="item.publishStatus" tone="success" />
          </template>
          <template #actions>
            <button class="button" type="button" :disabled="!item.sourceUrl" @click="openSource(item)">查看来源</button>
          </template>
          <template #extra>来源文件：{{ item.source || "无" }}</template>
        </RecordCard>
        <PaginationBar
          v-if="articleTotal > pageSize"
          :page-no="pageNo"
          :page-size="pageSize"
          :total="articleTotal"
          @change="changePage"
        />
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="模板与资源"
        description="模板列表已接入后端，支持上传和直接下载。"
      />

      <form class="form upload-form" @submit.prevent="handleUpload">
        <label>
          <span>资源名称</span>
          <input v-model="uploadForm.name" class="input" type="text" placeholder="例如 奖学金材料模板" />
        </label>
        <div class="form-grid">
          <label>
            <span>资源分类</span>
            <select v-model="uploadForm.categoryLabel" class="input input--select">
              <option value="证明">证明</option>
              <option value="奖助">奖助</option>
              <option value="党团">党团</option>
              <option value="学籍">学籍</option>
              <option value="就业">就业</option>
            </select>
          </label>
          <label>
            <span>资源类型</span>
            <select v-model="uploadForm.bizType" class="input input--select">
              <option value="kb_template">模板资源</option>
              <option value="kb_policy">政策文件</option>
              <option value="knowledge_attachment">知识库附件</option>
              <option value="application_attachment">申请附件</option>
              <option value="notice_attachment">通知附件</option>
            </select>
          </label>
        </div>
        <label>
          <span>资源说明</span>
          <textarea v-model="uploadForm.description" class="input textarea" rows="3" placeholder="填写资源用途、适用范围或版本说明"></textarea>
        </label>
        <label>
          <span>选择文件</span>
          <input class="input" type="file" accept=".pdf,.doc,.docx,.xls,.xlsx,.txt" @change="handleFileChange" />
        </label>
        <button class="button button--primary" type="submit" :disabled="uploading">
          {{ uploading ? "上传中..." : "上传资源" }}
        </button>
      </form>
      <p v-if="uploadFeedback" class="feedback">{{ uploadFeedback }}</p>

      <LoadingState v-if="loading" text="模板资源加载中..." />
      <ErrorState v-else-if="error" description="模板资源加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <div class="section-head">
          <h3>模板与资源</h3>
          <span class="subtle-note">共 {{ filteredTemplates.length }} 份</span>
        </div>
        <EmptyState
          v-if="!filteredTemplates.length"
          title="暂无模板资源"
          description="可以调整分类或关键字，重新筛选。"
        />
        <RecordCard
          v-for="item in filteredTemplates"
          :key="item.templateId"
          :meta="`${item.categoryLabel} · ${item.fileType.toUpperCase()} · ${item.updatedAt}`"
          :title="item.name"
          :description="item.description"
        >
          <template #actions>
            <button class="button" type="button" :disabled="!item.fileUrl" @click="downloadTemplate(item)">下载模板</button>
          </template>
        </RecordCard>
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
import { useAsyncPage } from "../../composables/useAsyncPage";
import { uploadFile } from "../../api/modules/fileApi";
import { createKnowledgeTemplate, getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";
import { downloadWithAuth } from "../../utils/downloadFile";

const articles = ref([]);
const templates = ref([]);
const articleTotal = ref(0);
const pageNo = ref(1);
const pageSize = 20;
const keyword = ref("");
const categoryFilter = ref("all");
const uploading = ref(false);
const uploadFeedback = ref("");
const selectedFile = ref(null);
const uploadForm = ref({
  name: "",
  categoryLabel: "证明",
  bizType: "kb_template",
  description: "",
});
const { loading, error, run } = useAsyncPage(async () => {
  const [articlePage, templateList] = await Promise.all([
    getKnowledgeList({ pageNo: pageNo.value, pageSize }),
    getKnowledgeTemplates(),
  ]);
  return { articleList: articlePage.records || [], articleTotal: Number(articlePage.total) || 0, templateList };
});

const categoryOptions = computed(() =>
  [...new Set(articles.value.map((item) => item.categoryLabel))],
);

const filteredArticles = computed(() =>
  articles.value.filter((item) => {
    const matchCategory = categoryFilter.value === "all" || item.categoryLabel === categoryFilter.value;
    const text = `${item.title} ${item.summary} ${item.source || ""}`.toLowerCase();
    const matchKeyword = !keyword.value || text.includes(keyword.value.toLowerCase());
    return matchCategory && matchKeyword;
  }),
);

const filteredTemplates = computed(() =>
  templates.value.filter((item) => {
    const matchCategory = categoryFilter.value === "all" || item.categoryLabel === categoryFilter.value;
    const text = `${item.name} ${item.description || ""} ${item.categoryLabel || ""} ${item.fileType || ""}`.toLowerCase();
    const matchKeyword = !keyword.value || text.includes(keyword.value.toLowerCase());
    return matchCategory && matchKeyword;
  }),
);

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const { articleList, articleTotal: total, templateList } = await run();
    articles.value = articleList;
    articleTotal.value = total;
    templates.value = templateList;
  } catch {}
}

async function changePage(nextPageNo) {
  pageNo.value = nextPageNo;
  await loadData();
}

async function openSource(item) {
  if (!item.sourceUrl) {
    return;
  }
  try {
    await downloadWithAuth(item.sourceUrl, item.source || "knowledge-source.txt");
  } catch (error) {
    window.alert(error?.message || "来源文件下载失败，请稍后重试。");
  }
}

async function downloadTemplate(item) {
  if (!item.fileUrl) {
    return;
  }
  try {
    await downloadWithAuth(item.fileUrl, `${item.name || "template"}.${item.fileType || "txt"}`);
  } catch (error) {
    window.alert(error?.message || "模板下载失败，请稍后重试。");
  }
}

function handleFileChange(event) {
  selectedFile.value = event.target.files?.[0] ?? null;
  if (selectedFile.value && !uploadForm.value.name.trim()) {
    uploadForm.value.name = selectedFile.value.name.replace(/\.[^.]+$/, "");
  }
}

async function handleUpload() {
  uploadFeedback.value = "";
  if (!selectedFile.value) {
    uploadFeedback.value = "请先选择要上传的文件。";
    return;
  }
  if (!uploadForm.value.name.trim()) {
    uploadFeedback.value = "请填写资源名称。";
    return;
  }

  uploading.value = true;
  try {
    const uploaded = await uploadFile(selectedFile.value, uploadForm.value.bizType);
    const fileType = inferFileType(uploaded.fileName || selectedFile.value.name);
    await createKnowledgeTemplate({
      name: uploadForm.value.name,
      categoryLabel: uploadForm.value.categoryLabel,
      fileType,
      description: uploadForm.value.description || "教师上传资源",
      fileId: uploaded.fileId,
    });
    uploadFeedback.value = `文件已上传：${uploaded.fileName}`;
    selectedFile.value = null;
    uploadForm.value = {
      name: "",
      categoryLabel: "证明",
      bizType: "kb_template",
      description: "",
    };
    await loadData();
  } catch (error) {
    uploadFeedback.value = error?.message || "文件上传失败，请稍后重试。";
  } finally {
    uploading.value = false;
  }
}

function inferFileType(fileName) {
  const extension = fileName.split(".").pop()?.toLowerCase();
  return extension || "file";
}
</script>

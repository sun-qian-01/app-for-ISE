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
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="模板与资源"
        description="模板列表已接入后端，支持直接下载。"
      />

      <LoadingState v-if="loading" text="模板资源加载中..." />
      <ErrorState v-else-if="error" description="模板资源加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <div class="section-head">
          <h3>模板与资源</h3>
          <span class="subtle-note">共 {{ templates.length }} 份</span>
        </div>
        <EmptyState
          v-if="!templates.length"
          title="暂无模板资源"
          description="后续可在这里接入上传、版本和启停用管理。"
        />
        <RecordCard
          v-for="item in templates"
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
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";
import { downloadWithAuth } from "../../utils/downloadFile";

const articles = ref([]);
const templates = ref([]);
const keyword = ref("");
const categoryFilter = ref("all");
const { loading, error, run } = useAsyncPage(async () => {
  const [articlePage, templateList] = await Promise.all([
    getKnowledgeList({ pageNo: 1, pageSize: 100 }),
    getKnowledgeTemplates(),
  ]);
  return { articleList: articlePage.records || [], templateList };
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

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const { articleList, templateList } = await run();
    articles.value = articleList;
    templates.value = templateList;
  } catch {}
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
</script>

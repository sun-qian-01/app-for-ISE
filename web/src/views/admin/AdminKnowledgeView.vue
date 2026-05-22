<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="知识库管理"
        api="GET /kb/articles"
        description="管理知识文章的分类、版本、发布状态和来源文件。"
      >
        <template #actions>
          <button class="button button--primary" type="button" @click="fillDemoForm">填充示例</button>
        </template>
      </PageHeader>

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
          description="可以调整分类与关键字，或后续接入新建文章表单。"
        />
        <RecordCard
          v-for="item in filteredArticles"
          :key="item.title"
          :meta="`${item.categoryLabel} · ${item.version}`"
          :title="item.title"
          :description="item.summary"
        >
          <template #tags>
            <StatusTag :label="item.publishStatus" tone="success" />
          </template>
          <template #actions>
            <button class="button" type="button" @click="editArticle(item)">编辑</button>
            <button class="button" type="button">查看来源</button>
          </template>
          <template #extra>来源文件：{{ item.source }}</template>
        </RecordCard>
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="文章编辑"
        api="POST /kb/articles"
        description="创建或编辑知识条目，并在下方维护模板与资源列表。"
      />

      <form class="form" @submit.prevent="saveArticle">
        <label>
          <span>文章标题</span>
          <input v-model="form.title" class="input" type="text" placeholder="例如：国家奖学金评定流程说明" />
        </label>
        <label>
          <span>分类</span>
          <select v-model="form.categoryLabel" class="input input--select">
            <option value="">请选择分类</option>
            <option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label>
          <span>版本号</span>
          <input v-model="form.version" class="input" type="text" placeholder="例如：v5" />
        </label>
        <label>
          <span>摘要</span>
          <textarea
            v-model="form.summary"
            class="input textarea"
            rows="4"
            placeholder="请输入条目摘要，说明可检索的核心内容"
          />
        </label>
        <label>
          <span>来源文件</span>
          <input v-model="form.source" class="input" type="text" placeholder="例如：国家奖学金评定办法.pdf" />
        </label>
        <label>
          <span>关键词</span>
          <input v-model="form.keywordsText" class="input" type="text" placeholder="多个关键词用中文顿号分隔，例如：奖学金、国家奖学金、评定" />
        </label>
        <div class="topbar__actions">
          <button class="button" type="button" @click="resetForm">重置</button>
          <button class="button button--primary" type="submit">
            {{ editingTitle ? "保存修改" : "创建条目" }}
          </button>
        </div>
      </form>

      <div class="stack create-panel__notes">
        <RecordCard
          meta="版本规则"
          title="已发布条目修改后应保留历史版本"
          description="当前先以 version 字段 mock 演示，后续接真实接口时再补版本列表和发布动作。"
        />
      </div>

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
          :key="item.name"
          :meta="`${item.categoryLabel} · ${item.fileType.toUpperCase()} · ${item.updatedAt}`"
          :title="item.name"
          :description="item.description"
        >
          <template #actions>
            <button class="button" type="button">替换文件</button>
          </template>
        </RecordCard>
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
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";

const articles = ref([]);
const templates = ref([]);
const keyword = ref("");
const categoryFilter = ref("all");
const editingTitle = ref("");
const form = reactive({
  title: "",
  categoryLabel: "",
  version: "",
  summary: "",
  source: "",
  keywordsText: "",
});
const { loading, error, run } = useAsyncPage(async () => {
  const [articleList, templateList] = await Promise.all([getKnowledgeList(), getKnowledgeTemplates()]);
  return { articleList, templateList };
});

const categoryOptions = computed(() =>
  [...new Set(articles.value.map((item) => item.categoryLabel))],
);

const filteredArticles = computed(() =>
  articles.value.filter((item) => {
    const matchCategory = categoryFilter.value === "all" || item.categoryLabel === categoryFilter.value;
    const text = `${item.title} ${item.summary} ${item.source}`.toLowerCase();
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

function fillDemoForm() {
  form.title = "毕业生就业信息登记补充说明";
  form.categoryLabel = "就业";
  form.version = "v1";
  form.summary = "说明毕业生就业信息补录时间、材料要求和联系方式核验规则。";
  form.source = "毕业生就业信息补录通知.docx";
  form.keywordsText = "就业、毕业生、补录、信息登记";
  editingTitle.value = "";
}

function editArticle(item) {
  form.title = item.title;
  form.categoryLabel = item.categoryLabel;
  form.version = item.version;
  form.summary = item.summary;
  form.source = item.source;
  form.keywordsText = item.keywords.join("、");
  editingTitle.value = item.title;
}

function resetForm() {
  form.title = "";
  form.categoryLabel = "";
  form.version = "";
  form.summary = "";
  form.source = "";
  form.keywordsText = "";
  editingTitle.value = "";
}

function saveArticle() {
  const title = form.title.trim();
  const categoryLabel = form.categoryLabel.trim();
  const version = form.version.trim();
  const summary = form.summary.trim();
  const source = form.source.trim();
  const keywords = form.keywordsText
    .split("、")
    .map((item) => item.trim())
    .filter(Boolean);

  if (!title || !categoryLabel || !version || !summary || !source) {
    return;
  }

  const nextItem = {
    title,
    categoryLabel,
    version,
    publishStatus: "published",
    summary,
    source,
    keywords,
  };

  if (editingTitle.value) {
    articles.value = articles.value.map((item) => (item.title === editingTitle.value ? nextItem : item));
  } else {
    articles.value = [nextItem, ...articles.value];
  }

  resetForm();
}
</script>

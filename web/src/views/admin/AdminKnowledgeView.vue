<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="知识库管理"
        api="GET /kb/articles"
        description="管理知识文章的分类、版本、发布状态和来源文件。"
      >
        <template #actions>
          <button class="button button--primary" type="button">新建文章</button>
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
            <button class="button" type="button">编辑</button>
            <button class="button" type="button">查看来源</button>
          </template>
          <template #extra>来源文件：{{ item.source }}</template>
        </RecordCard>
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="模板与资源"
        api="GET /kb/templates"
        description="集中维护学生下载模板、政策附件和参考资料。"
      />

      <LoadingState v-if="loading" text="模板资源加载中..." />
      <ErrorState v-else-if="error" description="模板资源加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
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
import { computed, onMounted, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";

const articles = ref([]);
const templates = ref([]);
const loading = ref(false);
const error = ref(false);
const keyword = ref("");
const categoryFilter = ref("all");

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
  loading.value = true;
  error.value = false;
  try {
    const [articleList, templateList] = await Promise.all([getKnowledgeList(), getKnowledgeTemplates()]);
    articles.value = articleList;
    templates.value = templateList;
  } catch (err) {
    console.error(err);
    error.value = true;
  } finally {
    loading.value = false;
  }
}
</script>

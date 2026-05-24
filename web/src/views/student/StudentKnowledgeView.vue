<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="智能问答"
        description="按学院已发布知识条目生成问答，若无可靠依据则明确提示未检索到依据。"
      />
      <form class="form" @submit.prevent="submitQuestion">
        <label>
          <span>问题描述</span>
          <textarea
            v-model="question"
            class="input textarea"
            rows="4"
            placeholder="例如：国家奖学金需要提交哪些材料？"
          />
        </label>
        <button class="button button--primary" type="submit">检索依据并生成回答</button>
      </form>

      <div v-if="answer" class="answer-box">
        <strong>回答</strong>
        <p>{{ answer }}</p>
        <div v-if="answerSources.length" class="stack answer-box__sources">
          <strong>依据来源</strong>
          <div class="tag-group">
            <StatusTag v-for="source in answerSources" :key="source" :label="source" />
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="政策与模板"
        description="支持按分类和关键字筛选文章，并查看可下载模板。"
      />
      <SearchBar>
        <select v-model="categoryFilter" class="input input--select">
          <option value="all">全部分类</option>
          <option v-for="item in categoryOptions" :key="item" :value="item">{{ item }}</option>
        </select>
        <input v-model="keyword" class="input" type="search" placeholder="搜索标题、摘要、来源" />
      </SearchBar>

      <LoadingState v-if="loading" text="知识库内容加载中..." />
      <ErrorState v-else-if="error" description="知识库列表加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <section class="template-panel">
          <div class="section-head">
            <h3>常用模板下载</h3>
            <span class="subtle-note">共 {{ templates.length }} 份</span>
          </div>
          <div class="stack">
            <EmptyState
              v-if="!templates.length"
              title="暂无模板"
              description="当前暂无可下载模板。"
            />
            <RecordCard
              v-for="item in templates"
              :key="item.templateId"
              :meta="`${item.categoryLabel} · ${item.fileType.toUpperCase()} · ${item.updatedAt}`"
              :title="item.name"
              :description="item.description"
            >
              <template #actions>
                <button class="button" type="button" :disabled="!item.fileUrl" @click="downloadTemplate(item)">
                  下载模板
                </button>
              </template>
            </RecordCard>
          </div>
        </section>

        <section class="stack">
          <div class="section-head">
            <h3>政策条目</h3>
            <span class="subtle-note">共 {{ filteredArticles.length }} 条</span>
          </div>
          <EmptyState
            v-if="!filteredArticles.length"
            title="没有匹配的知识条目"
            description="可以调整分类或关键字，重新筛选。"
          />
          <RecordCard
            v-for="item in filteredArticles"
            :key="item.articleId"
            :to="{ name: 'student-kb-article', params: { articleId: item.articleId } }"
            :meta="`${item.categoryLabel} · ${item.version}`"
            :title="item.title"
            :description="item.summary"
          >
            <template #tags>
              <StatusTag :label="item.publishStatus" tone="success" />
            </template>
            <template #extra>来源：{{ item.source || "无" }}</template>
          </RecordCard>
        </section>
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
import { askKnowledgeQuestion, getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";
import { downloadWithAuth } from "../../utils/downloadFile";

const question = ref("");
const answer = ref("");
const answerSources = ref([]);
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

async function submitQuestion() {
  const text = question.value.trim();
  if (!text) {
    answer.value = "请输入问题后再检索。";
    answerSources.value = [];
    return;
  }

  try {
    const result = await askKnowledgeQuestion(text);
    answer.value = result.answer;
    answerSources.value = (result.sources || []).map((item) =>
      `${item.title}${item.fileName ? ` · ${item.fileName}` : ""}`,
    );
  } catch (error) {
    answer.value = error?.message || "问答请求失败";
    answerSources.value = [];
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

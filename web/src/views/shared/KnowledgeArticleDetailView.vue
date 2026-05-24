<template>
  <section class="panel">
    <PageHeader :title="article.title || '知识条目详情'" :description="article.summary">
      <template #meta>
        <StatusTag v-if="article.publishStatus" :label="article.publishStatus" tone="success" />
        <StatusTag v-if="article.loadedFromFallback" label="摘要展示" />
      </template>
      <template #actions>
        <button class="button" type="button" @click="backToList">返回知识库</button>
      </template>
    </PageHeader>

    <LoadingState v-if="loading" text="文章内容加载中..." />
    <ErrorState v-else-if="error" :description="errorMessage" @retry="loadData" />

    <div v-else class="stack">
      <div class="info-list">
        <div><span>分类</span><strong>{{ article.categoryLabel }}</strong></div>
        <div><span>版本</span><strong>{{ article.version }}</strong></div>
        <div><span>浏览量</span><strong>{{ article.viewCount }}</strong></div>
        <div><span>来源</span><strong>{{ article.source || "无" }}</strong></div>
      </div>

      <section class="record">
        <h3>内容</h3>
        <p class="article-content">{{ article.content || "暂无正文内容。" }}</p>
      </section>

      <section v-if="article.keywords.length" class="record">
        <h3>关键词</h3>
        <div class="tag-group">
          <StatusTag v-for="item in article.keywords" :key="item" :label="item" />
        </div>
      </section>

      <div class="topbar__actions">
        <button class="button" type="button" :disabled="!article.sourceUrl" @click="openSource">
          下载来源文件
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { getKnowledgeArticleDetail, getKnowledgeList } from "../../api/modules/kbApi";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { downloadWithAuth } from "../../utils/downloadFile";

const route = useRoute();
const router = useRouter();

const article = reactive({
  title: "",
  summary: "",
  categoryLabel: "",
  version: "",
  content: "",
  source: "",
  sourceUrl: "",
  publishStatus: "",
  keywords: [],
  viewCount: 0,
  loadedFromFallback: false,
});

const { loading, error, errorDetail, run } = useAsyncPage(async () => {
  const articleId = Number(route.params.articleId);
  if (!Number.isFinite(articleId) || articleId <= 0) {
    throw new Error("invalid article id");
  }

  try {
    const detail = await getKnowledgeArticleDetail(articleId);
    return { detail, fallback: false };
  } catch (detailError) {
    if (canFallbackToList(detailError)) {
      const page = await getKnowledgeList({ pageNo: 1, pageSize: 200, publishStatus: "published" });
      const matched = (page.records || []).find((item) => Number(item.articleId) === articleId);
      if (matched) {
        return {
          detail: {
            title: matched.title,
            summary: matched.summary,
            categoryLabel: matched.categoryLabel,
            version: matched.version,
            content: matched.summary ? `${matched.summary}（当前显示为摘要内容）` : "暂无正文内容。",
            source: matched.source,
            sourceUrl: matched.sourceUrl,
            publishStatus: matched.publishStatus,
            keywords: matched.keywords || [],
            viewCount: 0,
          },
          fallback: true,
        };
      }
    }
    throw detailError;
  }
});

const errorMessage = computed(() => {
  const message = errorDetail.value?.message || "";
  const status = errorDetail.value?.status;
  const businessCode = errorDetail.value?.businessCode;
  if (status === 401 || businessCode === 40100 || message.includes("missing bearer token")) {
    return "登录状态已失效，请重新登录后再试。";
  }
  if (status === 404 || businessCode === 40400 || message.includes("article not found")) {
    return "文章不存在、未发布，或你暂无访问权限。";
  }
  if (status === 503 || (typeof status === "number" && status >= 500)) {
    return "文章服务暂不可用，请重启后端后重试。";
  }
  if (errorDetail.value?.code === "ERR_NETWORK") {
    return "后端服务未连接（127.0.0.1:8080），请先启动后端。";
  }
  return "文章加载失败，请稍后重试。";
});

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const result = await run();
    const detail = result.detail || result;
    article.title = detail.title || "";
    article.summary = detail.summary || "";
    article.categoryLabel = detail.categoryLabel || "";
    article.version = detail.version || "";
    article.content = detail.content || "";
    article.source = detail.source || "";
    article.sourceUrl = detail.sourceUrl || "";
    article.publishStatus = detail.publishStatus || "";
    article.keywords = detail.keywords || [];
    article.viewCount = detail.viewCount || 0;
    article.loadedFromFallback = Boolean(result.fallback);
  } catch {}
}

function canFallbackToList(detailError) {
  const status = detailError?.status;
  const businessCode = detailError?.businessCode;
  return (
    status === 404 ||
    status === 503 ||
    (typeof status === "number" && status >= 500) ||
    businessCode === 40400 ||
    (typeof businessCode === "number" && businessCode >= 50000)
  );
}

function backToList() {
  if (route.path.startsWith("/admin/")) {
    router.push("/admin/kb");
    return;
  }
  router.push("/student/kb");
}

async function openSource() {
  if (!article.sourceUrl) {
    return;
  }
  try {
    await downloadWithAuth(article.sourceUrl, article.source || "knowledge-source.txt");
  } catch (error) {
    window.alert(error?.message || "来源文件下载失败，请稍后重试。");
  }
}
</script>

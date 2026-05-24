<template>
  <section class="panel">
    <PageHeader :title="notice.title || '通知详情'" :description="notice.audience">
      <template #meta>
        <StatusTag :label="notice.read ? '已读' : '未读'" :tone="notice.read ? 'success' : 'warn'" />
      </template>
      <template #actions>
        <button class="button" type="button" @click="backToList">返回通知列表</button>
      </template>
    </PageHeader>

    <LoadingState v-if="loading" text="通知详情加载中..." />
    <ErrorState v-else-if="error" :description="errorMessage" @retry="loadData" />

    <div v-else class="stack">
      <div class="info-list">
        <div><span>发布时间</span><strong>{{ notice.publishAt || "-" }}</strong></div>
        <div><span>触达人数</span><strong>{{ notice.deliveredCount }}</strong></div>
        <div><span>已读人数</span><strong>{{ notice.readCount }}</strong></div>
      </div>

      <section class="record">
        <h3>通知正文</h3>
        <p class="article-content">{{ notice.content || "暂无正文内容。" }}</p>
      </section>

      <section v-if="notice.tags.length || notice.channelLabels.length" class="record">
        <h3>标签与渠道</h3>
        <div class="tag-group">
          <StatusTag v-for="item in notice.tags" :key="`tag-${item}`" :label="item" />
          <span v-for="item in notice.channelLabels" :key="`channel-${item}`" class="tag">{{ item }}</span>
        </div>
      </section>

      <div class="topbar__actions">
        <button
          v-if="!notice.read"
          class="button button--primary"
          type="button"
          @click="markAsRead"
        >
          标记已读
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
import { useAsyncPage } from "../../composables/useAsyncPage";
import { getMyNoticeDetail, markNoticeRead } from "../../api/modules/noticeApi";

const route = useRoute();
const router = useRouter();

const notice = reactive({
  id: 0,
  title: "",
  content: "",
  audience: "",
  publishAt: "",
  deliveredCount: 0,
  readCount: 0,
  tags: [],
  channelLabels: [],
  read: false,
});

const { loading, error, errorDetail, run } = useAsyncPage(async () => {
  const noticeId = Number(route.params.noticeId);
  if (!Number.isFinite(noticeId) || noticeId <= 0) {
    throw new Error("invalid notice id");
  }
  return getMyNoticeDetail(noticeId);
});

const errorMessage = computed(() => {
  const status = errorDetail.value?.status;
  const businessCode = errorDetail.value?.businessCode;
  if (status === 401 || businessCode === 40100) {
    return "登录状态已失效，请重新登录后再试。";
  }
  if (status === 404 || businessCode === 40400) {
    return "通知不存在或已下线。";
  }
  if (status === 503 || (typeof status === "number" && status >= 500)) {
    return "通知服务暂不可用，请稍后重试。";
  }
  if (errorDetail.value?.code === "ERR_NETWORK") {
    return "后端服务未连接（127.0.0.1:8080），请先启动后端。";
  }
  return "通知详情加载失败，请稍后重试。";
});

onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const data = await run();
    notice.id = data.id || 0;
    notice.title = data.title || "";
    notice.content = data.content || "";
    notice.audience = data.audience || "";
    notice.publishAt = data.publishAt || "";
    notice.deliveredCount = data.deliveredCount || 0;
    notice.readCount = data.readCount || 0;
    notice.tags = data.tags || [];
    notice.channelLabels = data.channelLabels || [];
    notice.read = data.readStatus === "read";
  } catch {}
}

async function markAsRead() {
  if (!notice.id || notice.read) {
    return;
  }
  try {
    await markNoticeRead(notice.id);
    await loadData();
  } catch {}
}

function backToList() {
  router.push("/student/notices");
}
</script>

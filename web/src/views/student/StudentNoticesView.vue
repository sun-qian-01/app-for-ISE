<template>
  <section class="panel">
    <PageHeader
      title="通知中心"
      description="集中查看定向通知、已读状态和触达渠道。"
    >
      <template #actions>
        <button class="button" type="button" @click="markAllRead">全部标记已读</button>
      </template>
    </PageHeader>

    <SearchBar>
      <select v-model="readFilter" class="input input--select">
        <option value="all">全部状态</option>
        <option value="unread">仅看未读</option>
        <option value="read">仅看已读</option>
      </select>
      <input v-model="keyword" class="input" type="search" placeholder="搜索通知标题、内容、标签" />
    </SearchBar>

    <LoadingState v-if="loading" text="通知列表加载中..." />
    <ErrorState v-else-if="error" description="通知列表加载失败，请稍后重试。" @retry="loadData" />
    <div v-else class="stack">
      <EmptyState
        v-if="!filteredItems.length"
        title="暂无匹配通知"
        description="可以调整筛选条件，或稍后查看新的触达记录。"
      />
      <RecordCard
        v-for="item in filteredItems"
        :key="item.id"
        :to="{ name: 'student-notice-detail', params: { noticeId: item.id } }"
        :meta="`${item.date} · ${item.audience}`"
        :title="item.title"
        :description="item.content"
        :tone="item.read ? '' : 'current'"
      >
        <template #tags>
          <StatusTag :label="item.statusLabel" :tone="item.read ? 'success' : 'warn'" />
          <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
          <span v-for="channel in item.channelLabels" :key="channel" class="tag">{{ channel }}</span>
        </template>
        <template #actions>
          <button v-if="!item.read" class="button button--primary" type="button" @click="markRead(item.id)">
            标记已读
          </button>
        </template>
        <template #extra>
          触达 {{ item.stats.delivered }} 人，已读 {{ item.stats.read }} 人
        </template>
      </RecordCard>
    </div>
  </section>
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
import { getMyNotices, markAllNoticesRead, markNoticeRead } from "../../api/modules/noticeApi";

const items = ref([]);
const keyword = ref("");
const readFilter = ref("all");
const { loading, error, run } = useAsyncPage(() => getMyNotices({ pageNo: 1, pageSize: 50 }));

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchRead =
      readFilter.value === "all" ||
      (readFilter.value === "read" && item.read) ||
      (readFilter.value === "unread" && !item.read);

    const text = `${item.title} ${item.content} ${item.tags.join(" ")}`.toLowerCase();
    const matchKeyword = !keyword.value || text.includes(keyword.value.toLowerCase());
    return matchRead && matchKeyword;
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
      date: item.publishAt,
      audience: item.audience,
      title: item.title,
      content: item.content,
      tags: item.tags || [],
      channelLabels: item.channelLabels || [],
      read: item.readStatus === "read",
      statusLabel: item.readStatus === "read" ? "已读" : "未读",
      stats: {
        delivered: item.deliveredCount || 0,
        read: item.readCount || 0,
      },
    }));
  } catch {}
}

async function markRead(id) {
  try {
    await markNoticeRead(id);
    await loadData();
  } catch {}
}

async function markAllRead() {
  try {
    await markAllNoticesRead();
    await loadData();
  } catch {}
}
</script>

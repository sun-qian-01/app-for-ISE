<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="精准通知"
        api="GET /notices"
        description="查看通知列表、触达统计和已读情况，并从右侧创建新的定向通知。"
      >
        <template #actions>
          <button class="button button--primary" type="button" @click="fillDemoForm">填充示例</button>
        </template>
      </PageHeader>

      <div class="grid grid--three">
        <MetricCard label="通知总数" :value="items.length" hint="已发布通知" />
        <MetricCard label="未读通知" :value="unreadCount" hint="面向学生仍未读的记录" />
        <MetricCard label="平均已读率" :value="`${averageReadRate}%`" hint="基于当前触达统计估算" text-mode />
      </div>

      <SearchBar>
        <select v-model="readFilter" class="input input--select">
          <option value="all">全部状态</option>
          <option value="unread">未读通知</option>
          <option value="read">已读通知</option>
        </select>
        <input v-model="keyword" class="input" type="search" placeholder="搜索标题、受众、标签" />
      </SearchBar>

      <LoadingState v-if="loading" text="通知管理数据加载中..." />
      <ErrorState v-else-if="error" description="通知管理数据加载失败，请稍后重试。" @retry="loadData" />
      <div v-else class="stack">
        <EmptyState
          v-if="!filteredItems.length"
          title="暂无匹配通知"
          description="可以调整筛选条件，或后续补充新的定向通知。"
        />
        <RecordCard
          v-for="item in filteredItems"
          :key="item.id"
          :meta="`${item.date} · ${item.audience}`"
          :title="item.title"
          :description="item.content"
        >
          <template #tags>
            <StatusTag :label="item.statusLabel" :tone="item.read ? 'success' : 'warn'" />
            <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
          </template>
          <template #extra>
            触达 {{ item.stats.delivered }} 人，已读 {{ item.stats.read }} 人，通过 {{ item.channelLabels.join(" / ") }} 发送
          </template>
        </RecordCard>
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="创建通知"
        api="POST /notices"
        description="按目标范围、标签和渠道配置通知，并直接提交后端。"
      />
      <form class="form" @submit.prevent="createNoticeRecord">
        <label>
          <span>通知标题</span>
          <input v-model="form.title" class="input" type="text" placeholder="例如：2026 届就业信息补录提醒" />
        </label>
        <label>
          <span>通知内容</span>
          <textarea
            v-model="form.content"
            class="input textarea"
            rows="5"
            placeholder="请输入通知正文、截止时间和办理说明"
          />
        </label>
        <label>
          <span>目标范围</span>
          <input v-model="form.audience" class="input" type="text" placeholder="例如：2026届毕业生 / 2022级 + 奖学金关注" />
        </label>
        <label>
          <span>标签</span>
          <input v-model="form.tagsText" class="input" type="text" placeholder="多个标签用中文顿号分隔，例如：就业、补录、信息校验" />
        </label>
        <div class="stack">
          <span>触达渠道</span>
          <div class="tag-group">
            <button
              v-for="channel in channelOptions"
              :key="channel"
              class="button"
              :class="{ 'button--primary': form.channels.includes(channel) }"
              type="button"
              @click="toggleChannel(channel)"
            >
              {{ channel }}
            </button>
          </div>
        </div>
        <div class="topbar__actions">
          <button class="button" type="button" @click="resetForm">重置</button>
          <button class="button button--primary" type="submit">立即创建</button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import MetricCard from "../../components/common/MetricCard.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { createNotice, getNoticeList } from "../../api/modules/noticeApi";

const items = ref([]);
const keyword = ref("");
const readFilter = ref("all");
const channelOptions = ["站内", "邮件", "微信"];
const form = reactive({
  title: "",
  content: "",
  audience: "",
  tagsText: "",
  channels: ["站内"],
});
const { loading, error, run } = useAsyncPage(() => getNoticeList({ pageNo: 1, pageSize: 100 }));

const unreadCount = computed(() => items.value.filter((item) => !item.read).length);

const averageReadRate = computed(() => {
  if (!items.value.length) return 0;
  const total = items.value.reduce((sum, item) => sum + item.stats.delivered, 0);
  const read = items.value.reduce((sum, item) => sum + item.stats.read, 0);
  return total ? Math.round((read / total) * 100) : 0;
});

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchRead =
      readFilter.value === "all" ||
      (readFilter.value === "read" && item.read) ||
      (readFilter.value === "unread" && !item.read);

    const text = `${item.title} ${item.audience} ${item.tags.join(" ")}`.toLowerCase();
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
      title: item.title,
      content: item.content,
      audience: item.audience,
      date: item.publishAt,
      channelLabels: item.channelLabels || [],
      read: item.readStatus === "read",
      statusLabel: item.readStatus === "read" ? "已读" : "未读",
      tags: item.tags || [],
      stats: {
        delivered: item.deliveredCount || 0,
        read: item.readCount || 0,
      },
    }));
  } catch {}
}

function toggleChannel(channel) {
  if (form.channels.includes(channel)) {
    form.channels = form.channels.filter((item) => item !== channel);
    return;
  }
  form.channels = [...form.channels, channel];
}

function resetForm() {
  form.title = "";
  form.content = "";
  form.audience = "";
  form.tagsText = "";
  form.channels = ["站内"];
}

function fillDemoForm() {
  form.title = "2026 届毕业生就业信息补录提醒";
  form.content = "请于本周五 18:00 前完成就业去向信息补录，并再次核对个人联系方式。";
  form.audience = "2026届毕业生";
  form.tagsText = "就业、补录、信息校验";
  form.channels = ["站内", "邮件", "微信"];
}

async function createNoticeRecord() {
  const title = form.title.trim();
  const content = form.content.trim();
  const audience = form.audience.trim();
  const tags = form.tagsText
    .split(/[、,，]/)
    .map((item) => item.trim())
    .filter(Boolean);

  if (!title || !content || !audience || !form.channels.length) {
    return;
  }

  try {
    await createNotice({
      title,
      content,
      audience,
      channelLabels: [...form.channels],
      tags,
    });
    resetForm();
    await loadData();
  } catch {}
}
</script>

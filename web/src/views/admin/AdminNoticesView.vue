<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="精准通知"
        api="GET /notices"
        description="查看通知列表、触达统计和已读情况，作为后续创建通知表单的承载页。"
      >
        <template #actions>
          <button class="button button--primary" type="button">创建通知</button>
        </template>
      </PageHeader>

      <div class="grid grid--three">
        <MetricCard label="通知总数" :value="items.length" hint="当前 mock 列表规模" />
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
          <template #actions>
            <button class="button" type="button">编辑</button>
            <button class="button" type="button">查看统计</button>
          </template>
          <template #extra>
            触达 {{ item.stats.delivered }} 人，已读 {{ item.stats.read }} 人，通过 {{ item.channelLabels.join(" / ") }} 发送
          </template>
        </RecordCard>
      </div>
    </section>

    <section class="panel">
      <PageHeader
        title="投放说明"
        description="这里保留通知范围和渠道配置的原型说明，后续可直接替换成真实表单。"
      />
      <div class="stack">
        <RecordCard
          meta="目标范围"
          title="支持按年级、班级、标签和党团阶段定向"
          description="前端表单建议与 docs/api.md 中的 targetRulesJson 保持一致，避免后端接入时重复改字段。"
        />
        <RecordCard
          meta="触达渠道"
          title="站内、邮件、微信三类渠道统一勾选"
          description="通知创建页应显示默认渠道和失败重试策略，管理页则以统计和状态回看为主。"
        />
        <RecordCard
          meta="后续扩展"
          title="预留附件、撤回和草稿态"
          description="当前静态原型已改成正式页面骨架，后续可以在这里继续补充表单抽屉和详情弹层。"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import MetricCard from "../../components/common/MetricCard.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { getNoticeList } from "../../api/modules/noticeApi";

const items = ref([]);
const loading = ref(false);
const error = ref(false);
const keyword = ref("");
const readFilter = ref("all");

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
  loading.value = true;
  error.value = false;
  try {
    items.value = await getNoticeList();
  } catch (err) {
    console.error(err);
    error.value = true;
  } finally {
    loading.value = false;
  }
}
</script>

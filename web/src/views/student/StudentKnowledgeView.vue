<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader
        title="智能问答"
        description="支持连续追问；会优先基于已发布知识条目回答。"
      />

      <div class="chat-box">
        <div ref="chatListRef" class="chat-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="chat-item"
            :class="[
              msg.role === 'user' ? 'chat-item--user' : 'chat-item--assistant',
              msg.error ? 'chat-item--error' : '',
            ]"
          >
            <div class="chat-role">{{ msg.role === "user" ? "我" : "助手" }}</div>
            <div class="chat-bubble">
              <div v-if="msg.role === 'assistant'" class="chat-bubble__meta">
                <StatusTag :label="msg.reliability.label" :tone="msg.reliability.tone" />
                <span v-if="typeof msg.confidence === 'number'" class="subtle-note">
                  置信度 {{ Math.round(msg.confidence * 100) }}%
                </span>
              </div>
              <p class="chat-bubble__content">{{ msg.content }}</p>
              <p v-if="msg.thinking" class="subtle-note">正在检索知识库并整理答案...</p>
              <p v-else-if="msg.role === 'assistant'" class="subtle-note">
                {{ msg.reliability.description }}
              </p>
              <div v-if="msg.sources?.length" class="qa-sources">
                <strong>依据来源</strong>
                <button
                  v-for="source in msg.sources"
                  :key="`${source.articleId || 'file'}-${source.sourceUrl || source.title}`"
                  class="source-chip"
                  type="button"
                  @click="openQaSource(source)"
                >
                  {{ formatQaSourceLabel(source) }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <form class="form" @submit.prevent="submitQuestion">
          <label>
            <span>问题描述</span>
            <textarea
              v-model="question"
              class="input textarea"
              rows="3"
              placeholder="例如：国家奖学金需要提交哪些材料？可继续追问：那截止时间呢？"
            />
          </label>
          <div class="topbar__actions">
            <button class="button button--primary" type="submit" :disabled="asking">
              {{ asking ? "思考中..." : "发送问题" }}
            </button>
            <button class="button" type="button" :disabled="asking" @click="clearConversation">清空对话</button>
          </div>
        </form>
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
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { askKnowledgeQuestionStream, getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";
import { createInitialKbChatMessages, loadKbChatMessages, saveKbChatMessages } from "../../utils/kbChatStore";
import { downloadWithAuth } from "../../utils/downloadFile";
import { formatQaSourceLabel, getQaReliability, normalizeQaSources } from "../../utils/kbQa";

const router = useRouter();
const question = ref("");
const asking = ref(false);
const chatListRef = ref(null);
const messageSeq = ref(1);
const idleReliability = {
  label: "可继续提问",
  tone: "default",
  description: "我会优先引用已发布知识条目；如果没有可靠来源，会明确提示。",
};
const messages = ref(createInitialKbChatMessages().map(prepareMessageForView));
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
  restoreConversation();
  loadData();
});

watch(messages, (value) => {
  saveKbChatMessages(value);
}, { deep: true });

async function loadData() {
  try {
    const { articleList, templateList } = await run();
    articles.value = articleList;
    templates.value = templateList;
  } catch {}
}

async function submitQuestion() {
  if (asking.value) {
    return;
  }
  const text = question.value.trim();
  if (!text) {
    return;
  }

  const history = buildHistory();
  const userMessageId = nextMessageId();
  messages.value.push({
    id: userMessageId,
    role: "user",
    content: text,
    sources: [],
    confidence: null,
    reliability: null,
    thinking: false,
    error: false,
  });
  scrollChatToBottom();
  question.value = "";

  const thinkingId = nextMessageId();
  messages.value.push({
    id: thinkingId,
    role: "assistant",
    content: "正在整理答案...",
    sources: [],
    confidence: null,
    reliability: {
      label: "检索中",
      tone: "default",
      description: "正在检索知识库并生成回答。",
    },
    thinking: true,
    error: false,
  });
  scrollChatToBottom();

  asking.value = true;
  try {
    const result = await askKnowledgeQuestionStream(text, {
      history,
      onEvent(event, data) {
        if (event === "status" && data?.message) {
          updateThinkingMessage(thinkingId, {
            content: data.message,
          });
        }
      },
    });
    const sources = normalizeQaSources(result.sources);
    replaceThinkingMessage(thinkingId, {
      content: result.answer || "未返回有效回答",
      sources,
      confidence: typeof result.confidence === "number" ? result.confidence : 0,
      error: false,
    });
  } catch (error) {
    replaceThinkingMessage(thinkingId, {
      content: error?.message || "问答请求失败",
      sources: [],
      confidence: 0,
      error: true,
    });
  } finally {
    asking.value = false;
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

function clearConversation() {
  messages.value = createInitialKbChatMessages().map((item) => prepareMessageForView({
    ...item,
    id: nextMessageId(),
    content: "对话已清空。你可以开始新的问题。",
  }));
  saveKbChatMessages(messages.value);
  scrollChatToBottom();
}

function buildHistory() {
  return messages.value
    .filter((item) => !item.thinking && (item.role === "user" || item.role === "assistant"))
    .slice(-10)
    .map((item) => ({ role: item.role, content: item.content }));
}

function replaceThinkingMessage(messageId, payload) {
  const index = messages.value.findIndex((item) => item.id === messageId);
  if (index < 0) {
    return;
  }
  const reliability = getQaReliability(payload.confidence, payload.sources);
  messages.value[index] = {
    id: messageId,
    role: "assistant",
    content: payload.content,
    sources: payload.sources,
    confidence: payload.confidence,
    reliability,
    thinking: false,
    error: payload.error,
  };
  saveKbChatMessages(messages.value);
  scrollChatToBottom();
}

function updateThinkingMessage(messageId, payload) {
  const index = messages.value.findIndex((item) => item.id === messageId);
  if (index < 0) {
    return;
  }
  messages.value[index] = {
    ...messages.value[index],
    ...payload,
  };
  scrollChatToBottom();
}

function restoreConversation() {
  const restored = loadKbChatMessages().map(prepareMessageForView);
  messages.value = restored.length ? restored : createInitialKbChatMessages().map(prepareMessageForView);
  messageSeq.value = Math.max(1, ...messages.value.map((item) => item.id || 1));
  scrollChatToBottom();
}

function prepareMessageForView(message) {
  const sources = normalizeQaSources(message.sources);
  const reliability = message.role === "assistant" && typeof message.confidence === "number"
    ? getQaReliability(message.confidence, sources)
    : idleReliability;
  return {
    ...message,
    sources,
    reliability: message.role === "assistant" ? reliability : null,
    thinking: Boolean(message.thinking),
    error: Boolean(message.error),
  };
}

function nextMessageId() {
  messageSeq.value += 1;
  return messageSeq.value;
}

function scrollChatToBottom() {
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight;
    }
  });
}

async function openQaSource(source) {
  if (source.articleId) {
    router.push({ name: "student-kb-article", params: { articleId: source.articleId } });
    return;
  }
  if (source.sourceUrl) {
    await downloadWithAuth(source.sourceUrl, source.fileName || source.title || "knowledge-source");
  }
}
</script>

<template>
  <div class="kb-ai-layout">
    <aside class="panel chat-history" :class="{ 'is-open': historyOpen }">
      <div class="section-head">
        <h2>聊天记录</h2>
        <button class="button button--primary" type="button" :disabled="asking" @click="createNewConversation">
          新对话
        </button>
      </div>
      <div class="chat-session-list">
        <button
          v-for="session in sortedSessions"
          :key="session.id"
          class="chat-session"
          :class="{ 'is-active': session.id === currentSessionId }"
          type="button"
          @click="selectConversation(session.id)"
        >
          <strong>{{ session.title }}</strong>
          <span>{{ getSessionPreview(session) }}</span>
          <small>{{ formatSessionTime(session.updatedAt) }}</small>
        </button>
      </div>
    </aside>
    <button v-if="historyOpen" class="history-backdrop" type="button" @click="historyOpen = false" />

    <div class="kb-ai-content">
      <section class="panel chat-panel">
        <PageHeader
          title="智能问答"
          description="您可以向 AI 智能体提出相关的问题。它将检索数据库里的文件，并为您提供答复。过程可能较慢，请耐心等待。"
        >
          <template #actions>
            <button class="button history-toggle" type="button" @click="historyOpen = true">聊天记录</button>
            <button class="button" type="button" :disabled="asking" @click="clearConversation">清空当前对话</button>
          </template>
        </PageHeader>

        <div class="chat-box">
          <div ref="chatListRef" class="chat-list chat-list--large">
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

          <form class="form chat-composer" @submit.prevent="submitQuestion">
            <label>
              <span>在这里输入您的问题</span>
              <textarea
                v-model="question"
                class="input textarea"
                rows="3"
                placeholder="我应该如何开具团员证明？"
                @keydown.shift.enter.prevent="submitQuestion"
              />
            </label>
            <div class="topbar__actions">
              <button class="button button--primary" type="submit" :disabled="asking">
                {{ asking ? "思考中..." : "发送问题" }}
              </button>
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
              <span class="subtle-note">共 {{ filteredTemplates.length }} 份</span>
            </div>
            <div class="stack">
              <EmptyState
                v-if="!filteredTemplates.length"
                title="暂无模板"
                description="可以调整分类或关键字，重新筛选。"
              />
              <RecordCard
                v-for="item in filteredTemplates"
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
            <PaginationBar
              v-if="articleTotal > pageSize"
              :page-no="pageNo"
              :page-size="pageSize"
              :total="articleTotal"
              @change="changePage"
            />
          </section>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import PaginationBar from "../../components/common/PaginationBar.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { askKnowledgeQuestionStream, getKnowledgeList, getKnowledgeTemplates } from "../../api/modules/kbApi";
import {
  createInitialKbChatMessages,
  createKbChatSession,
  getKbChatAccountKey,
  loadKbChatState,
  saveKbChatState,
} from "../../utils/kbChatStore";
import { useAuthStore } from "../../stores/auth";
import { downloadWithAuth } from "../../utils/downloadFile";
import { formatQaSourceLabel, getQaReliability, normalizeQaSources } from "../../utils/kbQa";

const router = useRouter();
const authStore = useAuthStore();
const question = ref("");
const asking = ref(false);
const chatListRef = ref(null);
const historyOpen = ref(false);
const sessions = ref([]);
const currentSessionId = ref("");
const idleReliability = {
  label: "",
  tone: "default",
  description: "我会优先引用已发布知识条目；如果没有可靠来源，会明确提示。",
};
const accountKey = computed(() => getKbChatAccountKey(authStore.user));
const currentSession = computed(() =>
  sessions.value.find((item) => item.id === currentSessionId.value) || sessions.value[0] || null,
);
const messages = computed(() => currentSession.value?.messages || []);
const sortedSessions = computed(() =>
  [...sessions.value].sort((a, b) => String(b.updatedAt || "").localeCompare(String(a.updatedAt || ""))),
);
const articles = ref([]);
const templates = ref([]);
const articleTotal = ref(0);
const pageNo = ref(1);
const pageSize = 20;
const keyword = ref("");
const categoryFilter = ref("all");
const { loading, error, run } = useAsyncPage(async () => {
  const [articlePage, templateList] = await Promise.all([
    getKnowledgeList({ pageNo: pageNo.value, pageSize }),
    getKnowledgeTemplates(),
  ]);
  return { articleList: articlePage.records || [], articleTotal: Number(articlePage.total) || 0, templateList };
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

const filteredTemplates = computed(() =>
  templates.value.filter((item) => {
    const matchCategory = categoryFilter.value === "all" || item.categoryLabel === categoryFilter.value;
    const text = `${item.name} ${item.description || ""} ${item.categoryLabel || ""} ${item.fileType || ""}`.toLowerCase();
    const matchKeyword = !keyword.value || text.includes(keyword.value.toLowerCase());
    return matchCategory && matchKeyword;
  }),
);

onMounted(() => {
  restoreConversation();
  loadData();
});

watch(accountKey, () => {
  restoreConversation();
});

watch([sessions, currentSessionId], () => {
  persistConversation();
}, { deep: true });

async function loadData() {
  try {
    const { articleList, articleTotal: total, templateList } = await run();
    articles.value = articleList;
    articleTotal.value = total;
    templates.value = templateList;
  } catch {}
}

async function changePage(nextPageNo) {
  pageNo.value = nextPageNo;
  await loadData();
}

async function submitQuestion() {
  if (asking.value) {
    return;
  }
  const text = question.value.trim();
  if (!text) {
    return;
  }

  const session = ensureActiveSession();
  const sessionId = session.id;
  const history = buildHistory(session.messages);
  session.messages.push({
    id: nextMessageId(session),
    role: "user",
    content: text,
    sources: [],
    confidence: null,
    reliability: null,
    thinking: false,
    error: false,
  });
  updateSessionMeta(session);
  scrollChatToBottom();
  question.value = "";

  const thinkingId = nextMessageId(session);
  session.messages.push({
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
  updateSessionMeta(session);
  scrollChatToBottom();

  asking.value = true;
  try {
    const result = await askKnowledgeQuestionStream(text, {
      history,
      onEvent(event, data) {
        if (event === "status" && data?.message) {
          updateThinkingMessage(sessionId, thinkingId, {
            content: data.message,
          });
        }
      },
    });
    const sources = normalizeQaSources(result.sources);
    replaceThinkingMessage(sessionId, thinkingId, {
      content: result.answer || "未返回有效回答",
      sources,
      confidence: typeof result.confidence === "number" ? result.confidence : 0,
      error: false,
    });
  } catch (error) {
    replaceThinkingMessage(sessionId, thinkingId, {
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

function createNewConversation() {
  const session = prepareSessionForView(createKbChatSession({ title: "新的对话" }));
  sessions.value = [session, ...sessions.value];
  currentSessionId.value = session.id;
  historyOpen.value = false;
  scrollChatToBottom();
}

function clearConversation() {
  const session = ensureActiveSession();
  session.messages = createInitialKbChatMessages().map(prepareMessageForView);
  session.title = "新的对话";
  updateSessionMeta(session);
  scrollChatToBottom();
}

function selectConversation(sessionId) {
  currentSessionId.value = sessionId;
  historyOpen.value = false;
  scrollChatToBottom();
}

function buildHistory(sourceMessages = messages.value) {
  return sourceMessages
    .filter((item) => !item.thinking && (item.role === "user" || item.role === "assistant"))
    .slice(-10)
    .map((item) => ({ role: item.role, content: item.content }));
}

function replaceThinkingMessage(sessionId, messageId, payload) {
  const session = findSession(sessionId);
  if (!session) {
    return;
  }
  const index = session.messages.findIndex((item) => item.id === messageId);
  if (index < 0) {
    return;
  }
  const reliability = getQaReliability(payload.confidence, payload.sources);
  session.messages[index] = {
    id: messageId,
    role: "assistant",
    content: payload.content,
    sources: payload.sources,
    confidence: payload.confidence,
    reliability,
    thinking: false,
    error: payload.error,
  };
  updateSessionMeta(session);
  scrollChatToBottom();
}

function updateThinkingMessage(sessionId, messageId, payload) {
  const session = findSession(sessionId);
  if (!session) {
    return;
  }
  const index = session.messages.findIndex((item) => item.id === messageId);
  if (index < 0) {
    return;
  }
  session.messages[index] = {
    ...session.messages[index],
    ...payload,
  };
  updateSessionMeta(session);
  scrollChatToBottom();
}

function restoreConversation() {
  const restored = loadKbChatState(accountKey.value);
  sessions.value = restored.sessions.map(prepareSessionForView);
  currentSessionId.value = restored.currentSessionId;
  scrollChatToBottom();
}

function persistConversation() {
  if (!sessions.value.length || !currentSessionId.value) {
    return;
  }
  saveKbChatState(accountKey.value, {
    currentSessionId: currentSessionId.value,
    sessions: sessions.value,
  });
}

function prepareSessionForView(session) {
  return {
    ...session,
    messages: session.messages.map(prepareMessageForView),
  };
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

function ensureActiveSession() {
  if (currentSession.value) {
    return currentSession.value;
  }
  createNewConversation();
  return currentSession.value;
}

function findSession(sessionId) {
  return sessions.value.find((item) => item.id === sessionId);
}

function updateSessionMeta(session) {
  session.updatedAt = new Date().toISOString();
  const firstQuestion = session.messages.find((item) => item.role === "user" && item.content);
  if (firstQuestion) {
    session.title = firstQuestion.content.length > 24 ? `${firstQuestion.content.slice(0, 24)}...` : firstQuestion.content;
  }
}

function nextMessageId(session) {
  return Math.max(0, ...session.messages.map((item) => Number(item.id) || 0)) + 1;
}

function getSessionPreview(session) {
  const latest = [...session.messages].reverse().find((item) => item.content && item.role !== "assistant");
  if (!latest) {
    return "尚未提问";
  }
  return latest.content.length > 32 ? `${latest.content.slice(0, 32)}...` : latest.content;
}

function formatSessionTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
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

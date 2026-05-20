<template>
  <div class="grid grid--two">
    <section class="panel">
      <PageHeader title="智能问答" api="POST /kb/qa" />
      <form class="form" @submit.prevent="submitQuestion">
        <label>
          <span>问题描述</span>
          <textarea v-model="question" class="input textarea" rows="4" placeholder="例如：国家奖学金需要提交哪些材料？" />
        </label>
        <button class="button button--primary" type="submit">检索依据并生成回答</button>
      </form>
      <div v-if="answer" class="answer-box">
        <strong>回答</strong>
        <p>{{ answer }}</p>
        <div v-if="answerSource" class="source-line">依据：{{ answerSource }}</div>
      </div>
      <p class="subtle-note">没有可靠来源时，回答应明确提示“未检索到可靠依据”。</p>
    </section>

    <section class="panel">
      <PageHeader title="政策与模板" />
      <SearchBar>
        <input v-model="keyword" class="input" type="search" placeholder="搜索标题、摘要、来源" />
      </SearchBar>
      <div class="stack">
        <EmptyState v-if="!filteredArticles.length" />
        <RecordCard
          v-for="item in filteredArticles"
          :key="item.title"
          :meta="`${item.categoryLabel} · ${item.version} · ${item.publishStatus}`"
          :title="item.title"
          :description="item.summary"
        >
          <template #extra>
            来源：{{ item.source }}
          </template>
        </RecordCard>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import RecordCard from "../../components/common/RecordCard.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import { fetchKnowledgeList } from "../../mocks/server";

const question = ref("");
const answer = ref("");
const answerSource = ref("");
const articles = ref([]);
const keyword = ref("");

const filteredArticles = computed(() =>
  articles.value.filter((item) => {
    if (!keyword.value) return true;
    const source = `${item.title} ${item.summary} ${item.source}`.toLowerCase();
    return source.includes(keyword.value.toLowerCase());
  }),
);

onMounted(async () => {
  articles.value = await fetchKnowledgeList();
});

function submitQuestion() {
  const matched = articles.value.find((item) => item.keywords.some((keyword) => question.value.includes(keyword)));
  if (!matched) {
    answer.value = "未检索到可靠依据。";
    answerSource.value = "";
    return;
  }
  answer.value = `${matched.summary} 具体办理以学院当年通知为准。`;
  answerSource.value = `${matched.title} · ${matched.source}`;
}
</script>

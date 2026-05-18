<template>
  <div class="grid grid--two">
    <section class="panel">
      <div class="section-head">
        <h2>智能问答</h2>
        <span class="pill">POST /kb/qa</span>
      </div>
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
      <div class="section-head">
        <h2>政策与模板</h2>
      </div>
      <div class="stack">
        <article v-for="item in articles" :key="item.title" class="knowledge-item">
          <div class="notice-card__meta">{{ item.categoryLabel }} · {{ item.version }} · {{ item.publishStatus }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
          <div class="source-line">来源：{{ item.source }}</div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchKnowledgeList } from "../../mocks/server";

const question = ref("");
const answer = ref("");
const answerSource = ref("");
const articles = ref([]);

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

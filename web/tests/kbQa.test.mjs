import assert from "node:assert/strict";
import {
  formatQaSourceLabel,
  getQaReliability,
  normalizeQaSources,
} from "../src/utils/kbQa.js";
import { parseSseEvents } from "../src/utils/sse.js";
import {
  createInitialKbChatMessages,
  loadKbChatMessages,
  saveKbChatMessages,
} from "../src/utils/kbChatStore.js";

const sources = normalizeQaSources([
  {
    articleId: 12,
    title: "国家奖学金评定流程说明",
    fileName: "国家奖学金.pdf",
    sourceUrl: "/api/v1/files/12/download",
  },
  {
    articleId: 13,
    title: "",
    fileName: "空标题来源.pdf",
  },
]);

assert.equal(formatQaSourceLabel(sources[0]), "国家奖学金评定流程说明 · 国家奖学金.pdf");
assert.equal(formatQaSourceLabel(sources[1]), "空标题来源.pdf");
assert.equal(getQaReliability(0.86, sources).tone, "success");
assert.equal(getQaReliability(0.34, sources).tone, "warn");
assert.equal(getQaReliability(0, []).label, "未检索到可靠依据");

const events = parseSseEvents(
  'event: route\n'
    + 'data: {"provider":"codex","model":"gpt-5.4-mini"}\n\n'
    + 'event: answer\n'
    + 'data: {"answer":"Codex answer","confidence":0.25,"sources":[]}\n\n',
);

assert.deepEqual(events, [
  { event: "route", data: { provider: "codex", model: "gpt-5.4-mini" } },
  { event: "answer", data: { answer: "Codex answer", confidence: 0.25, sources: [] } },
]);

const fakeStorage = createFakeStorage();
saveKbChatMessages([
  ...createInitialKbChatMessages(),
  { id: 2, role: "user", content: "国家奖学金材料？", sources: [], confidence: null, thinking: false, error: false },
  { id: 3, role: "assistant", content: "正在整理答案...", sources: [], confidence: null, thinking: true, error: false },
  { id: 4, role: "assistant", content: "需要提交申请表。", sources: sources.slice(0, 1), confidence: 0.8, thinking: false, error: false },
], fakeStorage);

const restoredMessages = loadKbChatMessages(fakeStorage);
assert.equal(restoredMessages.length, 3);
assert.equal(restoredMessages[1].content, "国家奖学金材料？");
assert.equal(restoredMessages.some((item) => item.thinking), false);
assert.equal(restoredMessages[2].sources[0].title, "国家奖学金评定流程说明");

const brokenStorage = createFakeStorage();
brokenStorage.setItem("ise_student_kb_chat_v1", "{broken");
assert.equal(loadKbChatMessages(brokenStorage)[0].content, createInitialKbChatMessages()[0].content);

function createFakeStorage() {
  const store = new Map();
  return {
    getItem(key) {
      return store.has(key) ? store.get(key) : null;
    },
    setItem(key, value) {
      store.set(key, value);
    },
  };
}

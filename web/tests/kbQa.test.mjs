import assert from "node:assert/strict";
import {
  formatQaSourceLabel,
  getQaReliability,
  normalizeQaSources,
} from "../src/utils/kbQa.js";

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

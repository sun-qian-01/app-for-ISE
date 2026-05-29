export function normalizeQaSources(sources = []) {
  if (!Array.isArray(sources)) {
    return [];
  }

  return sources
    .filter(Boolean)
    .map((source) => ({
      articleId: source.articleId ?? null,
      title: source.title || "",
      fileName: source.fileName || "",
      sourceUrl: source.sourceUrl || "",
    }))
    .filter((source) => source.articleId || source.title || source.fileName || source.sourceUrl);
}

export function formatQaSourceLabel(source) {
  if (!source) {
    return "未知来源";
  }
  const parts = [source.title, source.fileName].filter(Boolean);
  if (parts.length) {
    return parts.join(" · ");
  }
  return source.sourceUrl || "未知来源";
}

export function getQaReliability(confidence = 0, sources = []) {
  if (!sources.length || confidence <= 0) {
    return {
      label: "未检索到可靠依据",
      tone: "warn",
      description: "系统没有找到可引用的知识条目，请换一种问法或联系学院老师确认。",
    };
  }

  if (confidence >= 0.75) {
    return {
      label: "依据较充分",
      tone: "success",
      description: "回答已关联知识库来源，仍建议以原文通知为准。",
    };
  }

  if (confidence >= 0.45) {
    return {
      label: "已找到参考依据",
      tone: "default",
      description: "回答匹配到相关条目，但问题可能需要进一步明确。",
    };
  }

  return {
    label: "依据较弱",
    tone: "warn",
    description: "当前答案只匹配到弱相关来源，建议补充关键词后再次提问。",
  };
}

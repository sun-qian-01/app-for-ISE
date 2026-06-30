export const KB_CHAT_STORAGE_KEY = "ise_student_kb_chat_v1";

const MAX_STORED_MESSAGES = 30;

export function createInitialKbChatMessages() {
  return [
    {
      id: 1,
      role: "assistant",
      content: "你好，我是学院知识库助手。你可以直接问我政策流程，也可以连续追问。",
      sources: [],
      confidence: null,
      thinking: false,
      error: false,
    },
  ];
}

export function loadKbChatMessages(storage = globalThis.localStorage) {
  if (!storage) {
    return createInitialKbChatMessages();
  }
  try {
    const raw = storage.getItem(KB_CHAT_STORAGE_KEY);
    if (!raw) {
      return createInitialKbChatMessages();
    }
    const payload = JSON.parse(raw);
    const messages = normalizeMessages(payload?.messages);
    return messages.length ? messages : createInitialKbChatMessages();
  } catch {
    return createInitialKbChatMessages();
  }
}

export function saveKbChatMessages(messages, storage = globalThis.localStorage) {
  if (!storage) {
    return;
  }
  const stableMessages = normalizeMessages(messages)
    .filter((item) => !item.thinking)
    .slice(-MAX_STORED_MESSAGES)
    .map((item, index) => ({ ...item, id: index + 1 }));
  storage.setItem(KB_CHAT_STORAGE_KEY, JSON.stringify({
    version: 1,
    messages: stableMessages.length ? stableMessages : createInitialKbChatMessages(),
  }));
}

function normalizeMessages(messages) {
  if (!Array.isArray(messages)) {
    return [];
  }
  return messages
    .map((item, index) => normalizeMessage(item, index + 1))
    .filter(Boolean);
}

function normalizeMessage(item, fallbackId) {
  if (!item || (item.role !== "assistant" && item.role !== "user")) {
    return null;
  }
  const content = typeof item.content === "string" ? item.content.trim() : "";
  if (!content) {
    return null;
  }
  return {
    id: Number.isFinite(Number(item.id)) ? Number(item.id) : fallbackId,
    role: item.role,
    content,
    sources: Array.isArray(item.sources) ? item.sources : [],
    confidence: typeof item.confidence === "number" ? item.confidence : null,
    thinking: Boolean(item.thinking),
    error: Boolean(item.error),
  };
}

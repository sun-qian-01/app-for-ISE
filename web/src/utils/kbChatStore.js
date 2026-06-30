export const KB_CHAT_STORAGE_KEY = "ise_student_kb_chat_v1";
export const KB_CHAT_STORAGE_PREFIX = "ise_student_kb_chat_v2";

const MAX_STORED_MESSAGES = 40;
const MAX_STORED_SESSIONS = 12;

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

export function getKbChatAccountKey(user) {
  const raw = user?.username || user?.studentNo || user?.id || "guest";
  return String(raw).trim() || "guest";
}

export function createKbChatSession(payload = {}) {
  const now = new Date().toISOString();
  const messages = normalizeMessages(payload.messages || createInitialKbChatMessages());
  const title = normalizeTitle(payload.title) || deriveSessionTitle(messages);
  return {
    id: payload.id || createSessionId(),
    title,
    messages: messages.length ? messages : createInitialKbChatMessages(),
    createdAt: payload.createdAt || now,
    updatedAt: payload.updatedAt || now,
  };
}

export function loadKbChatState(accountKey, storage = globalThis.localStorage) {
  if (!storage) {
    return createInitialState();
  }
  try {
    const raw = storage.getItem(getStorageKey(accountKey));
    if (raw) {
      return normalizeState(JSON.parse(raw));
    }
    const legacyState = loadLegacyState(storage);
    if (legacyState) {
      return legacyState;
    }
    return createInitialState();
  } catch {
    return createInitialState();
  }
}

export function saveKbChatState(accountKey, state, storage = globalThis.localStorage) {
  if (!storage) {
    return;
  }
  const stableState = normalizeState(state);
  storage.setItem(getStorageKey(accountKey), JSON.stringify({
    version: 2,
    currentSessionId: stableState.currentSessionId,
    sessions: stableState.sessions,
  }));
}

export function createInitialState() {
  const session = createKbChatSession({ title: "新的对话" });
  return {
    currentSessionId: session.id,
    sessions: [session],
  };
}

export function getStorageKey(accountKey) {
  return `${KB_CHAT_STORAGE_PREFIX}:${encodeURIComponent(accountKey || "guest")}`;
}

function normalizeState(payload) {
  const sessions = Array.isArray(payload?.sessions)
    ? payload.sessions.map(normalizeSession).filter(Boolean).slice(0, MAX_STORED_SESSIONS)
    : [];
  const stableSessions = sessions.length ? sessions : createInitialState().sessions;
  const requestedCurrentId = String(payload?.currentSessionId || "");
  const currentSession = stableSessions.find((item) => item.id === requestedCurrentId) || stableSessions[0];
  return {
    currentSessionId: currentSession.id,
    sessions: stableSessions,
  };
}

function normalizeSession(item) {
  if (!item) {
    return null;
  }
  const messages = normalizeMessages(item.messages)
    .filter((message) => !message.thinking)
    .slice(-MAX_STORED_MESSAGES)
    .map((message, index) => ({ ...message, id: index + 1 }));
  const stableMessages = messages.length ? messages : createInitialKbChatMessages();
  return {
    id: String(item.id || createSessionId()),
    title: normalizeTitle(item.title) || deriveSessionTitle(stableMessages),
    messages: stableMessages,
    createdAt: typeof item.createdAt === "string" ? item.createdAt : new Date().toISOString(),
    updatedAt: typeof item.updatedAt === "string" ? item.updatedAt : new Date().toISOString(),
  };
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

function loadLegacyState(storage) {
  const raw = storage.getItem(KB_CHAT_STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    const payload = JSON.parse(raw);
    const messages = normalizeMessages(payload?.messages);
    if (!messages.length) {
      return null;
    }
    const session = createKbChatSession({
      title: deriveSessionTitle(messages),
      messages,
    });
    return {
      currentSessionId: session.id,
      sessions: [session],
    };
  } catch {
    return null;
  }
}

function deriveSessionTitle(messages) {
  const firstUserMessage = messages.find((item) => item.role === "user" && item.content);
  if (!firstUserMessage) {
    return "新的对话";
  }
  return normalizeTitle(firstUserMessage.content) || "新的对话";
}

function normalizeTitle(value) {
  const title = typeof value === "string" ? value.trim().replace(/\s+/g, " ") : "";
  if (!title) {
    return "";
  }
  return title.length > 24 ? `${title.slice(0, 24)}...` : title;
}

function createSessionId() {
  return `chat-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
}

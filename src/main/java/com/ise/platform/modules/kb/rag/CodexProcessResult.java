package com.ise.platform.modules.kb.rag;

public record CodexProcessResult(int exitCode, String stdout, String stderr, boolean timedOut) {
}

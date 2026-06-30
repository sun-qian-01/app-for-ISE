package com.ise.platform.modules.kb.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CodexQaProperties {

    @Value("${CODEX_QA_ENABLED:true}")
    private boolean enabled;

    @Value("${CODEX_QA_COMMAND:codex}")
    private String command;

    @Value("${CODEX_QA_MODEL:gpt-5.4-mini}")
    private String model;

    @Value("${CODEX_QA_WORKDIR:.}")
    private String workingDirectory;

    @Value("${CODEX_QA_TIMEOUT_MS:180000}")
    private long timeoutMs;

    @Value("${CODEX_QA_MAX_PROMPT_CHARS:16000}")
    private int maxPromptChars;

    public boolean isEnabled() {
        return enabled;
    }

    public String getCommand() {
        return StringUtils.hasText(command) ? command.trim() : "codex";
    }

    public String getModel() {
        return StringUtils.hasText(model) ? model.trim() : "gpt-5.4-mini";
    }

    public String getWorkingDirectory() {
        return StringUtils.hasText(workingDirectory) ? workingDirectory.trim() : ".";
    }

    public long getTimeoutMs() {
        return Math.max(5_000L, timeoutMs);
    }

    public int getMaxPromptChars() {
        return Math.max(2_000, maxPromptChars);
    }
}

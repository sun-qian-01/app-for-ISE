package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ise.platform.modules.kb.KbDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CodexQaClient implements QaAnswerClient {

    private static final Logger log = LoggerFactory.getLogger(CodexQaClient.class);

    private final CodexQaProperties properties;
    private final ObjectMapper objectMapper;
    private final CodexProcessRunner processRunner;

    public CodexQaClient(CodexQaProperties properties, ObjectMapper objectMapper, CodexProcessRunner processRunner) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.processRunner = processRunner;
    }

    @Override
    public String answer(String question,
                         List<KbDto.QaHistoryMessage> history,
                         List<RagChunk> evidence,
                         boolean allowGeneralReply) {
        if (!properties.isEnabled()) {
            return "";
        }

        List<String> command = buildCommand();
        String prompt = truncatePrompt(buildPrompt(question, history, evidence, allowGeneralReply));
        Duration timeout = Duration.ofMillis(properties.getTimeoutMs());
        CodexProcessResult result = processRunner.run(command, prompt, timeout);
        if (result.timedOut()) {
            log.warn("codex qa timed out after {} ms", timeout.toMillis());
            return "";
        }
        if (result.exitCode() != 0) {
            log.warn("codex qa failed, exitCode={}, stderr={}", result.exitCode(), abbreviate(result.stderr()));
            return "";
        }
        String answer = extractAnswer(result.stdout());
        if (!StringUtils.hasText(answer)) {
            log.warn("codex qa returned empty answer");
            return "";
        }
        String normalized = answer.trim();
        log.info("codex qa completed, answerLength={}, evidenceCount={}, allowGeneralReply={}",
            normalized.length(), evidence == null ? 0 : evidence.size(), allowGeneralReply);
        return normalized;
    }

    private List<String> buildCommand() {
        List<String> command = new ArrayList<>();
        for (String part : properties.getCommand().split("\\s+")) {
            if (!part.isBlank()) {
                command.add(part);
            }
        }
        command.add("exec");
        command.add("--json");
        command.add("--ephemeral");
        command.add("--sandbox");
        command.add("read-only");
        command.add("-m");
        command.add(properties.getModel());
        command.add("-C");
        command.add(properties.getWorkingDirectory());
        command.add("-");
        return command;
    }

    private String buildPrompt(String question,
                               List<KbDto.QaHistoryMessage> history,
                               List<RagChunk> evidence,
                               boolean allowGeneralReply) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是学院知识库助手。请只输出最终回答，不要输出思考过程。\n");
        builder.append("不要修改文件，不要执行会改变系统状态的命令；如需读取材料，只能读取当前工作目录下的相关内容。\n");
        appendMaterialRules(builder);
        if (allowGeneralReply) {
            builder.append("如果没有证据，你可以回答一般解释性问题；如果是学院政策、流程、材料、时间等具体问题且证据不足，必须说明未检索到可靠依据。\n");
        } else {
            builder.append("政策、流程、时间、材料等内容必须基于下面证据回答；证据不足时说明条目未提供该细节，不要编造。\n");
        }
        appendHistory(builder, history);
        appendEvidence(builder, evidence);
        builder.append("\n问题：").append(question == null ? "" : question.trim()).append("\n");
        return builder.toString();
    }

    private void appendHistory(StringBuilder builder, List<KbDto.QaHistoryMessage> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        builder.append("\n历史对话：\n");
        int start = Math.max(0, history.size() - 8);
        for (int i = start; i < history.size(); i++) {
            KbDto.QaHistoryMessage item = history.get(i);
            if (item == null || !StringUtils.hasText(item.getContent())) {
                continue;
            }
            String role = "assistant".equalsIgnoreCase(item.getRole()) ? "assistant" : "user";
            builder.append(role).append("：").append(item.getContent().trim()).append("\n");
        }
    }

    private void appendEvidence(StringBuilder builder, List<RagChunk> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            builder.append("\n证据：无\n");
            return;
        }
        builder.append("\n证据：\n");
        for (RagChunk chunk : evidence) {
            builder.append("[articleId=")
                .append(chunk.articleId())
                .append(", score=")
                .append(String.format(Locale.ROOT, "%.3f", chunk.score()))
                .append("] ")
                .append(nullToEmpty(chunk.title()))
                .append("\n分类：")
                .append(nullToEmpty(chunk.categoryLabel()))
                .append("\n来源：")
                .append(nullToEmpty(chunk.sourceFileName()))
                .append("\n内容：")
                .append(nullToEmpty(chunk.text()))
                .append("\n\n");
        }
    }

    private void appendMaterialRules(StringBuilder builder) {
        builder.append("材料使用规则：\n");
        builder.append("1. 知识库证据来自平台已处理的 material/ 原始材料，包括培养方案、综合类政策 PDF、党员证明和团员证明模板等；回答时以证据里的标题、分类、来源文件和内容为依据。\n");
        builder.append("2. 不要向用户暴露 Codex、模型名、降级链路、内部路径或工具调用细节；统一以“学院知识库助手”身份回答。\n");
        builder.append("3. 如果证据只给出摘要或办理原则，没有给出精确时间、学分、课程号、名额等细节，必须说明条目未提供该细节，并建议查看依据来源或咨询学院老师。\n");
        builder.append("4. 区分政策说明和模板文件：模板类材料只说明用途、填写要点和下载使用方式，不要把模板字段推断成正式审批结论。\n");
    }

    private String truncatePrompt(String prompt) {
        int max = properties.getMaxPromptChars();
        if (prompt.length() <= max) {
            return prompt;
        }
        return prompt.substring(0, max) + "\n\n[系统提示：上下文已按长度截断，请基于可见证据回答。]\n";
    }

    private String extractAnswer(String stdout) {
        if (!StringUtils.hasText(stdout)) {
            return "";
        }
        String last = "";
        boolean sawJson = false;
        for (String line : stdout.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                sawJson = true;
                String candidate = extractText(node);
                if (StringUtils.hasText(candidate) && isAnswerEvent(node)) {
                    last = candidate.trim();
                }
            } catch (Exception ignored) {
                if (!sawJson) {
                    last = trimmed;
                }
            }
        }
        return last;
    }

    private boolean isAnswerEvent(JsonNode node) {
        String type = node.path("type").asText("").toLowerCase(Locale.ROOT);
        if (type.contains("agent") || type.contains("assistant") || type.contains("message")) {
            return !type.contains("session.started");
        }
        String itemType = node.path("item").path("type").asText("").toLowerCase(Locale.ROOT);
        if (itemType.contains("agent") || itemType.contains("assistant") || itemType.contains("message")) {
            return true;
        }
        return node.hasNonNull("output_text") || node.hasNonNull("text");
    }

    private String extractText(JsonNode node) {
        for (String field : List.of("output_text", "message", "text", "delta")) {
            String value = node.path(field).asText("");
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        JsonNode payload = node.path("payload");
        if (!payload.isMissingNode()) {
            String value = extractText(payload);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        JsonNode item = node.path("item");
        if (!item.isMissingNode()) {
            String value = extractText(item);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        for (JsonNode content : node.path("content")) {
            String value = extractText(content);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String abbreviate(String value) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.length() <= 240) {
            return normalized;
        }
        return normalized.substring(0, 240);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

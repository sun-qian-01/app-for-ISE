package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ise.platform.modules.kb.KbDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Component
public class LlmResponsesClient {

    private final RagHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final KbRagProperties properties;

    public LlmResponsesClient(RagHttpClient httpClient, ObjectMapper objectMapper, KbRagProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String answer(String question,
                         List<KbDto.QaHistoryMessage> history,
                         List<RagChunk> evidence,
                         boolean allowGeneralReply) {
        String prompt = buildUserPrompt(question, evidence, allowGeneralReply);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getLlmModel());
        ArrayNode input = body.putArray("input");

        ObjectNode devMessage = input.addObject();
        devMessage.put("type", "message");
        devMessage.put("role", "developer");
        ArrayNode devContent = devMessage.putArray("content");
        devContent.addObject()
            .put("type", "input_text")
            .put("text", buildDeveloperPrompt(allowGeneralReply));

        appendHistory(input, history);

        ObjectNode userMessage = input.addObject();
        userMessage.put("type", "message");
        userMessage.put("role", "user");
        ArrayNode userContent = userMessage.putArray("content");
        userContent.addObject().put("type", "input_text").put("text", prompt);

        JsonNode response = httpClient.postJson(
            properties.getLlmBaseUrl() + "/responses",
            properties.getLlmApiKey(),
            body
        );

        String text = response.path("output_text").asText("").trim();
        if (!text.isEmpty()) {
            return text;
        }

        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                String candidate = content.path("text").asText("").trim();
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
        }

        return "未检索到可靠依据";
    }

    private void appendHistory(ArrayNode input, List<KbDto.QaHistoryMessage> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        int start = Math.max(0, history.size() - 8);
        for (int i = start; i < history.size(); i++) {
            KbDto.QaHistoryMessage item = history.get(i);
            if (item == null || !StringUtils.hasText(item.getContent())) {
                continue;
            }
            String role = normalizeRole(item.getRole());
            ObjectNode message = input.addObject();
            message.put("type", "message");
            message.put("role", role);
            ArrayNode content = message.putArray("content");
            content.addObject().put("type", "input_text").put("text", item.getContent().trim());
        }
    }

    private String buildDeveloperPrompt(boolean allowGeneralReply) {
        if (allowGeneralReply) {
            return "你是学院知识库助手。当前没有可引用的知识库证据时，也要直接回答用户的一般问题或解释性问题；如果用户询问具体学院政策、流程、时间、材料且缺少证据，必须说明未检索到可靠依据，并给出需要补充的关键词或建议咨询学院老师。回答简洁，不要输出思考过程。";
        }
        return "你是学院知识库问答助手。仅基于证据回答；证据不足时必须回复：未检索到可靠依据。请直接给最终回答，不要输出思考过程。";
    }

    private String buildUserPrompt(String question, List<RagChunk> evidence, boolean allowGeneralReply) {
        if (allowGeneralReply || evidence.isEmpty()) {
            return "问题：" + question;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("问题：").append(question).append("\n\n证据：\n");
        for (RagChunk chunk : evidence) {
            builder.append("[articleId=")
                .append(chunk.articleId())
                .append(",score=")
                .append(String.format("%.3f", chunk.score()))
                .append("] ")
                .append(chunk.title())
                .append("\n")
                .append(chunk.text())
                .append("\n\n");
        }
        return builder.toString();
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "user";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if ("assistant".equals(normalized)) {
            return "assistant";
        }
        return "user";
    }
}

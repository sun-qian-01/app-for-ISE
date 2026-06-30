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
public class LlmResponsesClient implements QaAnswerClient {

    private final RagHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final KbRagProperties properties;

    public LlmResponsesClient(RagHttpClient httpClient, ObjectMapper objectMapper, KbRagProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
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
        String materialRules = "材料使用规则：知识库证据来自平台已处理的 material/ 原始材料，包括培养方案、综合类政策 PDF、党员证明和团员证明模板等；回答时以证据里的标题、分类、来源文件和内容为依据。不要向用户暴露 Codex、模型名、降级链路、内部路径或工具调用细节，统一以“学院知识库助手”身份回答。若证据没有给出精确时间、学分、课程号、名额等细节，必须说明条目未提供该细节，并建议查看依据来源或咨询学院老师。模板类材料只说明用途、填写要点和下载使用方式，不要把模板字段推断成正式审批结论。";
        if (allowGeneralReply) {
            return "你是学院知识库助手。当前没有可引用的知识库证据时，也要直接回答用户的一般问题或解释性问题；如果用户询问具体学院政策、流程、时间、材料且缺少证据，必须说明未检索到可靠依据，并给出需要补充的关键词或建议咨询学院老师。回答简洁，不要输出思考过程。" + materialRules;
        }
        return "你是学院知识库问答助手。政策、流程、时间、材料等内容必须仅基于证据回答；证据不足时必须说明条目未提供该细节，不要编造。若问题同时询问你的身份，可以说明你是学院知识库助手。请直接给最终回答，不要输出思考过程。" + materialRules;
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
                .append("\n分类：")
                .append(chunk.categoryLabel())
                .append("\n来源文件：")
                .append(chunk.sourceFileName())
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

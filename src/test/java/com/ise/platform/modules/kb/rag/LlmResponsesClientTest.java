package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ise.platform.modules.kb.KbDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LlmResponsesClientTest {

    @Test
    void answerShouldIncludeMaterialRulesAndSourceMetadataInPrompt() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RagHttpClient httpClient = mock(RagHttpClient.class);
        KbRagProperties properties = readyProperties();
        LlmResponsesClient client = new LlmResponsesClient(httpClient, objectMapper, properties);
        RagChunk evidence = new RagChunk(
            "chunk-7",
            7L,
            "2025级大类培养方案解读",
            "2025级大类培养方案.pdf",
            12007L,
            "培养方案",
            "published",
            "2026-05-25",
            "2025级培养方案强调基础课程与方向模块衔接。",
            List.of(0.1d, 0.2d),
            0.91d
        );

        when(httpClient.postJson(eq("https://example.test/v1/responses"), eq("sk-test"), org.mockito.ArgumentMatchers.any(JsonNode.class)))
            .thenReturn(objectMapper.readTree("{\"output_text\":\"已基于培养方案回答。\"}"));

        String answer = client.answer("2025级怎么选课？", List.<KbDto.QaHistoryMessage>of(), List.of(evidence), false);

        assertThat(answer).isEqualTo("已基于培养方案回答。");
        ArgumentCaptor<JsonNode> bodyCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(httpClient).postJson(eq("https://example.test/v1/responses"), eq("sk-test"), bodyCaptor.capture());
        String body = bodyCaptor.getValue().toString();
        assertThat(body)
            .contains("知识库证据来自平台已处理的 material/ 原始材料")
            .contains("不要向用户暴露 Codex、模型名、降级链路、内部路径或工具调用细节")
            .contains("分类：培养方案")
            .contains("来源文件：2025级大类培养方案.pdf")
            .contains("2025级培养方案强调基础课程与方向模块衔接。");
    }

    private KbRagProperties readyProperties() {
        KbRagProperties properties = new KbRagProperties();
        ReflectionTestUtils.setField(properties, "llmBaseUrl", "https://example.test/v1");
        ReflectionTestUtils.setField(properties, "llmApiKey", "sk-test");
        ReflectionTestUtils.setField(properties, "llmModel", "gpt-5.4");
        return properties;
    }
}

package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmbeddingClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void embedShouldFallBackToLocalVectorWhenUpstreamEmbeddingFails() {
        KbRagProperties properties = ragProperties(16);
        RagHttpClient httpClient = mock(RagHttpClient.class);
        when(httpClient.postJson(eq("https://example.test/v1/embeddings"), eq("sk-test"), any(ObjectNode.class)))
            .thenThrow(new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed"));
        EmbeddingClient client = new EmbeddingClient(httpClient, properties, objectMapper);

        List<Double> vector = client.embed("国家奖学金申请材料");

        assertThat(vector).hasSize(16);
        assertThat(vector).anySatisfy(value -> assertThat(value).isNotZero());
    }

    @Test
    void localFallbackVectorShouldBeDeterministicAndNormalized() {
        KbRagProperties properties = ragProperties(32);
        RagHttpClient httpClient = mock(RagHttpClient.class);
        when(httpClient.postJson(eq("https://example.test/v1/embeddings"), eq("sk-test"), any(ObjectNode.class)))
            .thenThrow(new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed"));
        EmbeddingClient client = new EmbeddingClient(httpClient, properties, objectMapper);

        List<Double> first = client.embed("党员发展流程");
        List<Double> second = client.embed("党员发展流程");

        assertThat(first).isEqualTo(second);
        assertThat(vectorNorm(first)).isCloseTo(1.0d, org.assertj.core.data.Offset.offset(0.000001d));
    }

    @Test
    void embedShouldSkipUpstreamAfterFirstEmbeddingFailure() {
        KbRagProperties properties = ragProperties(16);
        RagHttpClient httpClient = mock(RagHttpClient.class);
        when(httpClient.postJson(eq("https://example.test/v1/embeddings"), eq("sk-test"), any(ObjectNode.class)))
            .thenThrow(new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed"));
        EmbeddingClient client = new EmbeddingClient(httpClient, properties, objectMapper);

        client.embed("团员");
        client.embed("党员");

        verify(httpClient, times(1))
            .postJson(eq("https://example.test/v1/embeddings"), eq("sk-test"), any(ObjectNode.class));
    }

    private KbRagProperties ragProperties(int dimension) {
        KbRagProperties properties = new KbRagProperties();
        ReflectionTestUtils.setField(properties, "llmBaseUrl", "https://example.test/v1");
        ReflectionTestUtils.setField(properties, "llmApiKey", "sk-test");
        ReflectionTestUtils.setField(properties, "embedBaseUrl", "https://example.test/v1");
        ReflectionTestUtils.setField(properties, "embedApiKey", "sk-test");
        ReflectionTestUtils.setField(properties, "vectorDimension", dimension);
        return properties;
    }

    private double vectorNorm(List<Double> vector) {
        double sum = 0;
        for (Double value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}

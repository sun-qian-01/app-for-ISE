package com.ise.platform.modules.kb.rag;

import com.ise.platform.modules.kb.KbDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KbRagServiceTest {

    @Test
    void qaShouldCallLlmWhenVectorSearchHasNoChunks() {
        KbRagProperties properties = readyProperties();
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        QdrantClient qdrantClient = mock(QdrantClient.class);
        QaAnswerClient qaAnswerClient = mock(QaAnswerClient.class);
        KbRagService service = new KbRagService(properties, embeddingClient, qdrantClient, qaAnswerClient);

        when(embeddingClient.embed("党员")).thenReturn(List.of(0.1d, 0.2d));
        when(qdrantClient.search(List.of(0.1d, 0.2d), properties.getTopK(), properties.getMinScore()))
            .thenReturn(List.of());
        when(qaAnswerClient.answer("党员", List.of(), List.of(), true))
            .thenReturn("党员一般指中国共产党党员。若咨询学院党员发展流程，请补充具体事项。");

        KbDto.QaResponse response = service.qa("党员", List.of());

        assertThat(response.getAnswer()).contains("党员");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.25d);
        verify(qaAnswerClient).answer("党员", List.of(), List.of(), true);
    }

    @Test
    void qaShouldCallLlmWhenEmbeddingReturnsEmptyVector() {
        KbRagProperties properties = readyProperties();
        EmbeddingClient embeddingClient = mock(EmbeddingClient.class);
        QdrantClient qdrantClient = mock(QdrantClient.class);
        QaAnswerClient qaAnswerClient = mock(QaAnswerClient.class);
        KbRagService service = new KbRagService(properties, embeddingClient, qdrantClient, qaAnswerClient);

        when(embeddingClient.embed("何意味？")).thenReturn(List.of());
        when(qaAnswerClient.answer("何意味？", List.of(), List.of(), true))
            .thenReturn("这句话可以理解为“是什么意思”。");

        KbDto.QaResponse response = service.qa("何意味？", List.of());

        assertThat(response.getAnswer()).contains("是什么意思");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.25d);
        verify(qaAnswerClient).answer("何意味？", List.of(), List.of(), true);
    }

    private KbRagProperties readyProperties() {
        KbRagProperties properties = new KbRagProperties();
        ReflectionTestUtils.setField(properties, "enabled", true);
        ReflectionTestUtils.setField(properties, "llmBaseUrl", "https://example.test/v1");
        ReflectionTestUtils.setField(properties, "llmApiKey", "sk-test");
        ReflectionTestUtils.setField(properties, "embedBaseUrl", "https://example.test/v1");
        ReflectionTestUtils.setField(properties, "embedApiKey", "sk-test");
        ReflectionTestUtils.setField(properties, "vectorEndpoint", "http://127.0.0.1:6333");
        return properties;
    }
}

package com.ise.platform.modules.kb.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FallbackQaAnswerClientTest {

    @Test
    void answerShouldPreferCodexWhenItReturnsText() {
        CodexQaClient codexQaClient = mock(CodexQaClient.class);
        LlmResponsesClient llmResponsesClient = mock(LlmResponsesClient.class);
        FallbackQaAnswerClient client = new FallbackQaAnswerClient(codexQaClient, llmResponsesClient);

        when(codexQaClient.answer("hello", List.of(), List.of(), true)).thenReturn("Codex answer");

        String answer = client.answer("hello", List.of(), List.of(), true);

        assertThat(answer).isEqualTo("Codex answer");
        verifyNoInteractions(llmResponsesClient);
    }

    @Test
    void answerShouldUseResponsesFallbackWhenCodexReturnsBlank() {
        CodexQaClient codexQaClient = mock(CodexQaClient.class);
        LlmResponsesClient llmResponsesClient = mock(LlmResponsesClient.class);
        FallbackQaAnswerClient client = new FallbackQaAnswerClient(codexQaClient, llmResponsesClient);

        when(codexQaClient.answer("hello", List.of(), List.of(), true)).thenReturn("");
        when(llmResponsesClient.answer("hello", List.of(), List.of(), true)).thenReturn("API fallback");

        String answer = client.answer("hello", List.of(), List.of(), true);

        assertThat(answer).isEqualTo("API fallback");
        verify(llmResponsesClient).answer("hello", List.of(), List.of(), true);
    }
}

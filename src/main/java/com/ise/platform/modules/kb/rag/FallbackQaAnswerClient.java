package com.ise.platform.modules.kb.rag;

import com.ise.platform.modules.kb.KbDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Primary
@Component
public class FallbackQaAnswerClient implements QaAnswerClient {

    private static final Logger log = LoggerFactory.getLogger(FallbackQaAnswerClient.class);

    private final CodexQaClient codexQaClient;
    private final LlmResponsesClient llmResponsesClient;

    public FallbackQaAnswerClient(CodexQaClient codexQaClient, LlmResponsesClient llmResponsesClient) {
        this.codexQaClient = codexQaClient;
        this.llmResponsesClient = llmResponsesClient;
    }

    @Override
    public String answer(String question,
                         List<KbDto.QaHistoryMessage> history,
                         List<RagChunk> evidence,
                         boolean allowGeneralReply) {
        try {
            String codexAnswer = codexQaClient.answer(question, history, evidence, allowGeneralReply);
            if (StringUtils.hasText(codexAnswer)) {
                return codexAnswer;
            }
        } catch (RuntimeException ex) {
            log.warn("codex qa failed before fallback, reason={}", ex.getMessage());
        }
        return llmResponsesClient.answer(question, history, evidence, allowGeneralReply);
    }
}

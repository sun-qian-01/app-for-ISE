package com.ise.platform.modules.kb.rag;

import com.ise.platform.modules.kb.KbDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Service
public class KbRagService {

    private final KbRagProperties properties;
    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;
    private final LlmResponsesClient llmResponsesClient;

    public KbRagService(KbRagProperties properties,
                        EmbeddingClient embeddingClient,
                        QdrantClient qdrantClient,
                        LlmResponsesClient llmResponsesClient) {
        this.properties = properties;
        this.embeddingClient = embeddingClient;
        this.qdrantClient = qdrantClient;
        this.llmResponsesClient = llmResponsesClient;
    }

    public boolean enabled() {
        return properties.readyForRag();
    }

    public KbDto.QaResponse qa(String question, List<KbDto.QaHistoryMessage> history) {
        List<Double> queryVector = embeddingClient.embed(question);
        if (queryVector.isEmpty()) {
            return answerWithoutEvidence(question, history);
        }

        List<RagChunk> chunks = qdrantClient.search(queryVector, properties.getTopK(), properties.getMinScore());
        if (chunks.isEmpty()) {
            return answerWithoutEvidence(question, history);
        }

        List<RagChunk> evidence = chunks.subList(0, Math.min(properties.getMaxContextChunks(), chunks.size()));
        String answer = llmResponsesClient.answer(question, history, evidence, false);
        List<KbDto.QaSource> sources = toSources(chunks, properties.getMaxSources());

        double confidence = averageScore(evidence);
        if (answer == null || answer.isBlank()) {
            answer = "未检索到可靠依据";
            confidence = 0;
        }
        return new KbDto.QaResponse(answer, sources, round(Math.min(0.97, confidence)));
    }

    private KbDto.QaResponse answerWithoutEvidence(String question, List<KbDto.QaHistoryMessage> history) {
        String answer = llmResponsesClient.answer(question, history, List.of(), true);
        if (!StringUtils.hasText(answer)) {
            answer = "未检索到可靠依据";
            return new KbDto.QaResponse(answer, List.of(), 0.0);
        }
        double confidence = isGeneralChat(question) ? 0.75 : 0.25;
        return new KbDto.QaResponse(answer, List.of(), confidence);
    }

    private List<KbDto.QaSource> toSources(List<RagChunk> chunks, int maxSources) {
        Map<Long, KbDto.QaSource> deduplicated = new LinkedHashMap<>();
        for (RagChunk chunk : chunks) {
            if (deduplicated.size() >= maxSources) {
                break;
            }
            deduplicated.computeIfAbsent(chunk.articleId(), key -> new KbDto.QaSource(
                chunk.articleId(),
                chunk.title(),
                chunk.sourceFileName(),
                buildSourceUrl(chunk.sourceFileId())
            ));
        }
        return new ArrayList<>(deduplicated.values());
    }

    private String buildSourceUrl(Long fileId) {
        if (fileId == null) {
            return "";
        }
        return "/api/v1/files/" + fileId + "/download";
    }

    private double averageScore(List<RagChunk> chunks) {
        if (chunks.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (RagChunk chunk : chunks) {
            total += chunk.score();
        }
        return total / chunks.size();
    }

    private double round(double value) {
        return Math.round(value * 1000.0d) / 1000.0d;
    }

    private boolean isGeneralChat(String question) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String text = question.toLowerCase(Locale.ROOT);
        return text.contains("你是谁")
            || text.contains("你叫什么")
            || text.contains("你能做什么")
            || text.contains("你会什么")
            || text.contains("你好")
            || "hi".equals(text.trim())
            || "hello".equals(text.trim());
    }
}

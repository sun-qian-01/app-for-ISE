package com.ise.platform.modules.kb.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KbRagProperties {

    @Value("${RAG_ENABLED:false}")
    private boolean enabled;

    @Value("${RAG_TOP_K:20}")
    private int topK;

    @Value("${RAG_MIN_SCORE:0.35}")
    private double minScore;

    @Value("${RAG_MAX_CONTEXT_CHUNKS:8}")
    private int maxContextChunks;

    @Value("${RAG_MAX_SOURCES:5}")
    private int maxSources;

    @Value("${RAG_LLM_BASE_URL:https://gmn.chuangzuoli.com/v1}")
    private String llmBaseUrl;

    @Value("${RAG_LLM_API_KEY:}")
    private String llmApiKey;

    @Value("${RAG_LLM_MODEL:gpt-5.2}")
    private String llmModel;

    @Value("${RAG_EMBED_BASE_URL:}")
    private String embedBaseUrl;

    @Value("${RAG_EMBED_API_KEY:}")
    private String embedApiKey;

    @Value("${RAG_EMBED_MODEL:text-embedding-3-small}")
    private String embedModel;

    @Value("${RAG_VECTOR_ENDPOINT:http://127.0.0.1:6333}")
    private String vectorEndpoint;

    @Value("${RAG_VECTOR_API_KEY:}")
    private String vectorApiKey;

    @Value("${RAG_VECTOR_COLLECTION:kb_article_chunks_v1}")
    private String vectorCollection;

    @Value("${RAG_VECTOR_DIMENSION:1536}")
    private int vectorDimension;

    @Value("${RAG_CONNECT_TIMEOUT_MS:5000}")
    private int connectTimeoutMs;

    @Value("${RAG_READ_TIMEOUT_MS:15000}")
    private int readTimeoutMs;

    @Value("${RAG_INDEX_BATCH_SIZE:32}")
    private int indexBatchSize;

    public boolean isEnabled() {
        return enabled;
    }

    public int getTopK() {
        return topK;
    }

    public double getMinScore() {
        return minScore;
    }

    public int getMaxContextChunks() {
        return maxContextChunks;
    }

    public int getMaxSources() {
        return maxSources;
    }

    public String getLlmBaseUrl() {
        return trimTrailingSlash(llmBaseUrl);
    }

    public String getLlmApiKey() {
        return llmApiKey;
    }

    public String getLlmModel() {
        return llmModel;
    }

    public String getEmbedBaseUrl() {
        if (StringUtils.hasText(embedBaseUrl)) {
            return trimTrailingSlash(embedBaseUrl);
        }
        return trimTrailingSlash(llmBaseUrl);
    }

    public String getEmbedApiKey() {
        if (StringUtils.hasText(embedApiKey)) {
            return embedApiKey;
        }
        return llmApiKey;
    }

    public String getEmbedModel() {
        return embedModel;
    }

    public String getVectorEndpoint() {
        return trimTrailingSlash(vectorEndpoint);
    }

    public String getVectorApiKey() {
        return vectorApiKey;
    }

    public String getVectorCollection() {
        return vectorCollection;
    }

    public int getVectorDimension() {
        return vectorDimension;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public int getIndexBatchSize() {
        return Math.max(1, indexBatchSize);
    }

    public boolean readyForRag() {
        return enabled
            && StringUtils.hasText(getLlmApiKey())
            && StringUtils.hasText(getLlmBaseUrl())
            && StringUtils.hasText(getEmbedApiKey())
            && StringUtils.hasText(getVectorEndpoint());
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}

package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class QdrantClient {

    private final RagHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final KbRagProperties properties;

    public QdrantClient(RagHttpClient httpClient, ObjectMapper objectMapper, KbRagProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void ensureCollection() {
        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode vectors = body.putObject("vectors");
        vectors.put("size", properties.getVectorDimension());
        vectors.put("distance", "Cosine");
        ObjectNode hnsw = body.putObject("hnsw_config");
        hnsw.put("m", 16);
        hnsw.put("ef_construct", 128);

        String url = properties.getVectorEndpoint() + "/collections/" + properties.getVectorCollection();
        try {
            httpClient.putJson(url, properties.getVectorApiKey(), body);
        } catch (BusinessException ex) {
            // Collection may already exist. Probe once; if probe fails then keep failing.
            httpClient.getJson(url, properties.getVectorApiKey());
        }
    }

    public void upsert(List<RagChunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode points = body.putArray("points");

        for (RagChunk chunk : chunks) {
            ObjectNode point = points.addObject();
            point.put("id", toUuid(chunk.chunkId()));

            ArrayNode vector = point.putArray("vector");
            for (Double value : chunk.vector()) {
                vector.add(value);
            }

            ObjectNode payload = point.putObject("payload");
            payload.put("chunk_id", chunk.chunkId());
            payload.put("article_id", chunk.articleId());
            payload.put("title", chunk.title());
            payload.put("source_file_name", chunk.sourceFileName());
            if (chunk.sourceFileId() != null) {
                payload.put("source_file_id", chunk.sourceFileId());
            }
            payload.put("category_label", chunk.categoryLabel());
            payload.put("publish_status", chunk.publishStatus());
            payload.put("updated_at", chunk.updatedAt());
            payload.put("chunk_text", chunk.text());
        }

        JsonNode response = httpClient.postJson(
            properties.getVectorEndpoint() + "/collections/" + properties.getVectorCollection() + "/points?wait=true",
            properties.getVectorApiKey(),
            body
        );
        if (!response.path("status").asText("").equalsIgnoreCase("ok")
            && !response.path("result").path("status").asText().equalsIgnoreCase("acknowledged")) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "qdrant upsert failed");
        }
    }

    public List<RagChunk> search(List<Double> queryVector, int limit, double minScore) {
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode vector = body.putArray("vector");
        for (Double value : queryVector) {
            vector.add(value);
        }
        body.put("limit", limit);
        body.put("with_payload", true);
        body.put("with_vector", false);

        ObjectNode filter = body.putObject("filter");
        ArrayNode must = filter.putArray("must");
        ObjectNode condition = must.addObject();
        condition.put("key", "publish_status");
        ObjectNode match = condition.putObject("match");
        match.put("value", "published");

        JsonNode response = httpClient.postJson(
            properties.getVectorEndpoint() + "/collections/" + properties.getVectorCollection() + "/points/search",
            properties.getVectorApiKey(),
            body
        );

        ArrayNode result = (ArrayNode) response.path("result");
        List<RagChunk> chunks = new ArrayList<>();
        for (JsonNode item : result) {
            double score = item.path("score").asDouble(0);
            if (score < minScore) {
                continue;
            }
            JsonNode payload = item.path("payload");
            chunks.add(new RagChunk(
                payload.path("chunk_id").asText(),
                payload.path("article_id").asLong(),
                payload.path("title").asText(),
                payload.path("source_file_name").asText(""),
                payload.path("source_file_id").isMissingNode() ? null : payload.path("source_file_id").asLong(),
                payload.path("category_label").asText(""),
                payload.path("publish_status").asText(""),
                payload.path("updated_at").asText(""),
                payload.path("chunk_text").asText(""),
                List.of(),
                score
            ));
        }
        return chunks;
    }

    private String toUuid(String rawId) {
        return UUID.nameUUIDFromBytes(rawId.getBytes(StandardCharsets.UTF_8)).toString();
    }
}

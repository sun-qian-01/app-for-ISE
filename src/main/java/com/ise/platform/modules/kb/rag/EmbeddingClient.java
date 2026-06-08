package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ise.platform.common.error.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EmbeddingClient {

    private final RagHttpClient httpClient;
    private final KbRagProperties properties;
    private final ObjectMapper objectMapper;
    private volatile boolean upstreamUnavailable;

    public EmbeddingClient(RagHttpClient httpClient, KbRagProperties properties, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public List<Double> embed(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        if (upstreamUnavailable) {
            return localEmbedding(text);
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getEmbedModel());
        body.put("input", text);

        JsonNode response;
        try {
            response = httpClient.postJson(
                properties.getEmbedBaseUrl() + "/embeddings",
                properties.getEmbedApiKey(),
                body
            );
        } catch (BusinessException ex) {
            upstreamUnavailable = true;
            return localEmbedding(text);
        }

        ArrayNode data = (ArrayNode) response.path("data");
        if (data.isEmpty()) {
            return localEmbedding(text);
        }

        JsonNode embeddingNode = data.get(0).path("embedding");
        List<Double> vector = new ArrayList<>(embeddingNode.size());
        for (JsonNode value : embeddingNode) {
            vector.add(value.asDouble());
        }
        if (vector.isEmpty()) {
            return localEmbedding(text);
        }
        return vector;
    }

    private List<Double> localEmbedding(String text) {
        int dimension = Math.max(1, properties.getVectorDimension());
        double[] vector = new double[dimension];
        String normalized = text.trim().toLowerCase();

        addFeature(vector, normalized, 1.0d);
        int[] codePoints = normalized.codePoints().toArray();
        for (int i = 0; i < codePoints.length; i++) {
            addFeature(vector, new String(codePoints, i, 1), 1.0d);
            if (i + 1 < codePoints.length) {
                addFeature(vector, new String(codePoints, i, 2), 1.5d);
            }
            if (i + 2 < codePoints.length) {
                addFeature(vector, new String(codePoints, i, 3), 1.2d);
            }
        }

        double norm = norm(vector);
        if (norm == 0) {
            vector[Math.floorMod(stableHash(normalized), dimension)] = 1.0d;
            norm = 1.0d;
        }

        List<Double> result = new ArrayList<>(dimension);
        for (double value : vector) {
            result.add(value / norm);
        }
        return result;
    }

    private void addFeature(double[] vector, String feature, double weight) {
        if (!StringUtils.hasText(feature)) {
            return;
        }
        int hash = stableHash(feature);
        int index = Math.floorMod(hash, vector.length);
        double sign = (hash & 1) == 0 ? 1.0d : -1.0d;
        vector[index] += sign * weight;
    }

    private int stableHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return ((bytes[0] & 0xff) << 24)
                | ((bytes[1] & 0xff) << 16)
                | ((bytes[2] & 0xff) << 8)
                | (bytes[3] & 0xff);
        } catch (NoSuchAlgorithmException ex) {
            return Arrays.hashCode(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private double norm(double[] vector) {
        double sum = 0;
        for (double value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}

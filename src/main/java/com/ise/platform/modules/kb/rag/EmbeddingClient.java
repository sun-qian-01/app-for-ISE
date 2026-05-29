package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmbeddingClient {

    private final RagHttpClient httpClient;
    private final KbRagProperties properties;
    private final ObjectMapper objectMapper;

    public EmbeddingClient(RagHttpClient httpClient, KbRagProperties properties, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public List<Double> embed(String text) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getEmbedModel());
        body.put("input", text);

        JsonNode response = httpClient.postJson(
            properties.getEmbedBaseUrl() + "/embeddings",
            properties.getEmbedApiKey(),
            body
        );

        ArrayNode data = (ArrayNode) response.path("data");
        if (data.isEmpty()) {
            return List.of();
        }

        JsonNode embeddingNode = data.get(0).path("embedding");
        List<Double> vector = new ArrayList<>(embeddingNode.size());
        for (JsonNode value : embeddingNode) {
            vector.add(value.asDouble());
        }
        return vector;
    }
}

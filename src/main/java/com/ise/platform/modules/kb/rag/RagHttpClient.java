package com.ise.platform.modules.kb.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RagHttpClient {

    private final ObjectMapper objectMapper;
    private final KbRagProperties properties;

    public RagHttpClient(ObjectMapper objectMapper, KbRagProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public JsonNode postJson(String url, String bearerKey, JsonNode requestBody) {
        return requestJson(url, bearerKey, requestBody, HttpMethod.POST);
    }

    public JsonNode putJson(String url, String bearerKey, JsonNode requestBody) {
        return requestJson(url, bearerKey, requestBody, HttpMethod.PUT);
    }

    public JsonNode getJson(String url, String bearerKey) {
        RestTemplate restTemplate = buildTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerKey != null && !bearerKey.isBlank()) {
            headers.setBearerAuth(bearerKey);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "empty rag response");
            }
            return objectMapper.readTree(body);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag response parse failed");
        }
    }

    private JsonNode requestJson(String url, String bearerKey, JsonNode requestBody, HttpMethod method) {
        RestTemplate restTemplate = buildTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerKey != null && !bearerKey.isBlank()) {
            headers.setBearerAuth(bearerKey);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "empty rag response");
            }
            return objectMapper.readTree(body);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag response parse failed");
        }
    }

    private RestTemplate buildTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }
}

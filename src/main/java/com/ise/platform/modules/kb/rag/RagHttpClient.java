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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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
        headers.set(HttpHeaders.USER_AGENT, "app-for-ise-rag/1.0");
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
        } catch (HttpStatusCodeException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, upstreamErrorMessage(e));
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag response parse failed");
        }
    }

    private JsonNode requestJson(String url, String bearerKey, JsonNode requestBody, HttpMethod method) {
        RestTemplate restTemplate = buildTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.USER_AGENT, "app-for-ise-rag/1.0");
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
        } catch (HttpStatusCodeException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, upstreamErrorMessage(e));
        } catch (RestClientException e) {
            return requestJsonWithCurl(url, bearerKey, requestBody, method, e);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag response parse failed");
        }
    }

    private RestTemplate buildTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setProxy(Proxy.NO_PROXY);
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    private String upstreamErrorMessage(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        if (body.length() > 240) {
            body = body.substring(0, 240);
        }
        return "rag upstream request failed: status=" + e.getStatusCode().value() + ", body=" + body;
    }

    private JsonNode requestJsonWithCurl(String url,
                                         String bearerKey,
                                         JsonNode requestBody,
                                         HttpMethod method,
                                         RestClientException originalError) {
        if (!url.startsWith("https://")
            || !(HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method))
            || bearerKey == null
            || bearerKey.isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream request failed: " + originalError.getMessage());
        }
        Path bodyFile = null;
        try {
            bodyFile = Files.createTempFile("app-for-ise-rag-", ".json");
            Files.writeString(bodyFile, requestBody.toString(), StandardCharsets.UTF_8);

            Process process = new ProcessBuilder("curl", "--config", "-").start();
            String config = """
                noproxy = "*"
                silent
                show-error
                fail-with-body
                max-time = "%d"
                request = "%s"
                url = "%s"
                header = "Content-Type: application/json"
                header = "User-Agent: app-for-ise-rag/1.0"
                header = "Authorization: Bearer %s"
                data-binary = "@%s"
                """.formatted(
                Math.max(5, properties.getReadTimeoutMs() / 1000 + 5),
                method.name(),
                url,
                bearerKey == null ? "" : bearerKey,
                bodyFile
            );
            process.getOutputStream().write(config.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();

            boolean finished = process.waitFor(Math.max(10, properties.getReadTimeoutMs() / 1000 + 10), TimeUnit.SECONDS);
            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream curl fallback timed out");
            }
            if (process.exitValue() != 0) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream curl fallback failed: " + abbreviate(stdout + " " + stderr));
            }
            if (stdout.isBlank()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "empty rag response");
            }
            return objectMapper.readTree(stdout);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream curl fallback unavailable");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "rag upstream curl fallback interrupted");
        } finally {
            if (bodyFile != null) {
                try {
                    Files.deleteIfExists(bodyFile);
                } catch (IOException ignored) {
                    // best-effort cleanup for transient request payload
                }
            }
        }
    }

    private String abbreviate(String value) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.length() <= 240) {
            return normalized;
        }
        return normalized.substring(0, 240);
    }
}

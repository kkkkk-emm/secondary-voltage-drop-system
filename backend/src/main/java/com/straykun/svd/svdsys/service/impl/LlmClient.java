package com.straykun.svd.svdsys.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 大模型客户端（OpenAI 兼容 /chat/completions）
 */
@Component
public class LlmClient {

    @Value("${llm.base-url:}")
    private String baseUrl;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:}")
    private String model;

    @Value("${llm.timeout-seconds:30}")
    private long timeoutSeconds;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public LlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 兼容旧 7 字段抽取，基于 22 字段映射得到
     */
    public Map<String, String> extractPairs(String ocrText) {
        Map<String, String> detailed = extractDetailedPairs(ocrText);
        return mapDetailedToLegacy(detailed);
    }

    /**
     * 从 OCR 文本中抽取固定 22 字段
     */
    public Map<String, String> extractDetailedPairs(String ocrText) {
        validateConfig();

        String endpoint = buildChatCompletionsEndpoint(baseUrl);
        String prompt = buildDetailedPrompt(ocrText);
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(Math.max(5, timeoutSeconds)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("大模型调用失败，HTTP状态码: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("大模型返回内容为空");
            }
            return parseModelOutput(content, OcrExtractFieldConstants.DETAILED_KEYS, true);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("解析大模型响应失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("大模型请求被中断", e);
        } catch (IOException e) {
            throw new IllegalStateException("大模型请求失败", e);
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("大模型配置缺失，请检查 llm.base-url");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException("大模型配置缺失，请检查 llm.api-key");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("大模型配置缺失，请检查 llm.model");
        }
    }

    private String buildChatCompletionsEndpoint(String configuredBaseUrl) {
        String trimmed = configuredBaseUrl.trim();
        if (trimmed.endsWith("/chat/completions")) {
            return trimmed;
        }
        if (trimmed.endsWith("/")) {
            return trimmed + "chat/completions";
        }
        return trimmed + "/chat/completions";
    }

    private String buildRequestBody(String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0);

        Map<String, String> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "你是结构化信息抽取助手。你必须只输出一个JSON对象，严禁输出解释、前后缀、Markdown代码块。");

        Map<String, String> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);

        body.put("messages", new Object[]{systemMsg, userMsg});
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("构造大模型请求失败", e);
        }
    }

    private String buildDetailedPrompt(String ocrText) {
        String requiredKeysText = formatKeysForPrompt(OcrExtractFieldConstants.DETAILED_KEYS);
        return """
                请从下面OCR文本中抽取固定22字段，输出严格JSON对象。
                要求：
                1) 只能输出JSON对象，不得输出解释、注释、Markdown。
                2) JSON必须包含以下全部键，键名必须完全一致：
                   """ + requiredKeysText + """
                3) 若某字段不存在，值填空字符串 ""。
                4) 值统一输出为字符串。
                5) 字段映射提示：
                   - "一次电压" 可能被OCR识别成 "二次电压"，仍映射到 "一次电压" 键
                   - "tanφ" 可能识别为 "tan"
                   - "温度"、"湿度" 可能被拆分为两行（如 "温" + "度"）
                   - 三相矩阵中 AO/BO/CO 对应每行三列
                   - 行标签可能出现为 f(%)、f%)、d(分)、dU(%)、Upt:U/UptU、Uyb:U/UybU
                OCR文本如下：
                ---
                """ + ocrText + """

                ---
                现在请直接输出JSON对象：
                """;
    }

    private Map<String, String> parseModelOutput(String modelOutput, String[] requiredKeys, boolean strictJsonErrorMessage) {
        String cleaned = cleanupModelOutput(modelOutput);
        JsonNode json;
        try {
            json = objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            if (strictJsonErrorMessage) {
                throw new IllegalStateException("大模型返回无法解析JSON: " + e.getOriginalMessage(), e);
            }
            throw new IllegalStateException("解析大模型返回内容失败", e);
        }
        if (!json.isObject()) {
            throw new IllegalStateException("大模型返回无法解析JSON: 返回内容不是JSON对象");
        }

        Map<String, String> pairs = new LinkedHashMap<>();
        for (String key : requiredKeys) {
            pairs.put(key, json.path(key).asText(""));
        }
        return pairs;
    }

    private Map<String, String> mapDetailedToLegacy(Map<String, String> detailed) {
        Map<String, String> pairs = OcrExtractFieldConstants.newLegacyMap();
        pairs.put(OcrExtractFieldConstants.LEGACY_DEVICE_NO, safeGet(detailed, OcrExtractFieldConstants.METER_POINT_ID));
        pairs.put(OcrExtractFieldConstants.LEGACY_TEST_DATE, safeGet(detailed, OcrExtractFieldConstants.TEST_DATE));
        pairs.put(OcrExtractFieldConstants.LEGACY_A_DROP, safeGet(detailed, OcrExtractFieldConstants.AO_DU));
        pairs.put(OcrExtractFieldConstants.LEGACY_B_DROP, safeGet(detailed, OcrExtractFieldConstants.BO_DU));
        pairs.put(OcrExtractFieldConstants.LEGACY_C_DROP, safeGet(detailed, OcrExtractFieldConstants.CO_DU));
        return pairs;
    }

    private String safeGet(Map<String, String> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return "";
        }
        return map.get(key) == null ? "" : map.get(key);
    }

    private String formatKeysForPrompt(String[] keys) {
        StringJoiner joiner = new StringJoiner(",");
        for (String key : keys) {
            joiner.add("\"" + key + "\"");
        }
        return joiner.toString();
    }

    private String cleanupModelOutput(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (!StringUtils.hasText(trimmed)) {
            return trimmed;
        }

        // 兼容部分模型返回 ```json ... ``` 包裹的情况
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewLine > -1 && lastFence > firstNewLine) {
                trimmed = trimmed.substring(firstNewLine + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}

package com.straykun.svd.svdsys.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 百度 OCR 客户端。
 */
@Component
public class BaiduOcrClient {

    @Value("${baidu.ocr.api-key:}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key:}")
    private String secretKey;

    @Value("${baidu.ocr.endpoint:https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic}")
    private String endpoint;

    @Value("${baidu.ocr.token-url:https://aip.baidubce.com/oauth/2.0/token}")
    private String tokenUrl;

    @Value("${baidu.ocr.token-refresh-buffer-seconds:120}")
    private long tokenRefreshBufferSeconds;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ReentrantLock tokenLock = new ReentrantLock();

    private volatile String accessToken;
    private volatile long tokenExpireAtEpochSecond;

    /**
     * 构造函数，初始化 BaiduOcrClient 所需依赖。
     *
     * @param objectMapper 参数 objectMapper。
     */
    public BaiduOcrClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 执行 recognize 业务逻辑。
     *
     * @param file 参数 file。
     * @return 返回处理结果。
     */
    public JsonNode recognize(MultipartFile file) {
        String token = getAccessToken();
        String imageBase64;
        try {
            imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("读取上传图片失败", e);
        }

        String formBody = "image=" + urlEncode(imageBase64);
        String requestUrl = endpoint + "?access_token=" + urlEncode(token);
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("百度OCR调用失败，HTTP状态码: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (root.hasNonNull("error_code") && root.path("error_code").asInt() != 0) {
                String errorMsg = root.path("error_msg").asText("未知错误");
                int errorCode = root.path("error_code").asInt();
                throw new IllegalStateException("百度OCR调用失败: " + errorMsg + " (error_code=" + errorCode + ")");
            }
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("解析百度OCR响应失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("百度OCR请求被中断", e);
        }
    }

    /**
     * 执行 extractText 数据处理。
     *
     * @param rawOcr 参数 rawOcr。
     * @return 返回字符串结果。
     */
    public String extractText(JsonNode rawOcr) {
        JsonNode wordsResult = rawOcr.path("words_result");
        if (!wordsResult.isArray()) {
            throw new IllegalStateException("百度OCR返回缺少 words_result");
        }

        List<String> lines = new ArrayList<>();
        for (JsonNode item : wordsResult) {
            String words = item.path("words").asText("");
            if (StringUtils.hasText(words)) {
                lines.add(words.trim());
            }
        }

        String text = String.join("\n", lines).trim();
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("OCR未识别到有效文字");
        }
        return text;
    }

    private String getAccessToken() {
        long now = System.currentTimeMillis() / 1000;
        if (StringUtils.hasText(accessToken) && now < (tokenExpireAtEpochSecond - tokenRefreshBufferSeconds)) {
            return accessToken;
        }

        tokenLock.lock();
        try {
            now = System.currentTimeMillis() / 1000;
            if (StringUtils.hasText(accessToken) && now < (tokenExpireAtEpochSecond - tokenRefreshBufferSeconds)) {
                return accessToken;
            }

            if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(secretKey)) {
                throw new IllegalArgumentException("百度OCR配置缺失，请检查 baidu.ocr.api-key 和 baidu.ocr.secret-key");
            }

            String formBody = "grant_type=client_credentials"
                    + "&client_id=" + urlEncode(apiKey)
                    + "&client_secret=" + urlEncode(secretKey);

            HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("获取百度OCR access_token失败，HTTP状态码: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String newToken = root.path("access_token").asText("");
            long expiresIn = root.path("expires_in").asLong(0);
            if (!StringUtils.hasText(newToken) || expiresIn <= 0) {
                String error = root.path("error_description").asText(root.path("error").asText("未知错误"));
                throw new IllegalStateException("获取百度OCR access_token失败: " + error);
            }

            this.accessToken = newToken;
            this.tokenExpireAtEpochSecond = now + expiresIn;
            return newToken;
        } catch (IOException e) {
            throw new IllegalStateException("解析百度OCR token响应失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取百度OCR token请求被中断", e);
        } finally {
            tokenLock.unlock();
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

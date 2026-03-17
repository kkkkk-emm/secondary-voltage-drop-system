package com.straykun.svd.svdsys.controller.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OCR提取响应响应视图对象。
 */
@Data
public class OcrExtractResponseVO {

    /**
     * OCR 拼接文本。
     */
    private String ocrText;

    private Map<String, String> pairs = new LinkedHashMap<>();

    private Map<String, String> detailedPairs = new LinkedHashMap<>();

    /**
     * OCR 原始响应。
     */
    private JsonNode rawOcr;
}

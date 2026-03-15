package com.straykun.svd.svdsys.controller.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图片 OCR 提取结果
 */
@Data
public class OcrExtractResponseVO {

    /**
     * OCR 拼接后的纯文本
     */
    private String ocrText;

    /**
     * 兼容旧版的 7 字段键值对
     */
    private Map<String, String> pairs = new LinkedHashMap<>();

    /**
     * 22 个明细字段键值对（规则优先 + 大模型兜底）
     */
    private Map<String, String> detailedPairs = new LinkedHashMap<>();

    /**
     * 百度 OCR 原始响应（调试用）
     */
    private JsonNode rawOcr;
}

package com.straykun.svd.svdsys.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.straykun.svd.svdsys.controller.vo.OcrExtractResponseVO;
import com.straykun.svd.svdsys.service.OcrExtractService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * OCR + 大模型字段抽取服务实现
 */
@Service
public class OcrExtractServiceImpl implements OcrExtractService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");

    @Value("${ocr.max-image-size:5242880}")
    private long maxImageSize;

    private final BaiduOcrClient baiduOcrClient;
    private final OcrTemplateRuleParser ocrTemplateRuleParser;
    private final LlmClient llmClient;

    public OcrExtractServiceImpl(BaiduOcrClient baiduOcrClient,
                                 OcrTemplateRuleParser ocrTemplateRuleParser,
                                 LlmClient llmClient) {
        this.baiduOcrClient = baiduOcrClient;
        this.ocrTemplateRuleParser = ocrTemplateRuleParser;
        this.llmClient = llmClient;
    }

    @Override
    public OcrExtractResponseVO extract(MultipartFile file) {
        validateImageFile(file);

        JsonNode rawOcr = baiduOcrClient.recognize(file);
        String ocrText = baiduOcrClient.extractText(rawOcr);

        Map<String, String> ruleDetailedPairs = ocrTemplateRuleParser.extractDetailedPairs(ocrText);
        Map<String, String> finalDetailedPairs = ruleDetailedPairs;
        if (hasBlankDetailedField(ruleDetailedPairs)) {
            Map<String, String> llmDetailedPairs = llmClient.extractDetailedPairs(ocrText);
            finalDetailedPairs = mergeDetailedPairs(ruleDetailedPairs, llmDetailedPairs);
        }
        Map<String, String> legacyPairs = mapDetailedToLegacyPairs(finalDetailedPairs);

        OcrExtractResponseVO vo = new OcrExtractResponseVO();
        vo.setOcrText(ocrText);
        vo.setPairs(legacyPairs);
        vo.setDetailedPairs(finalDetailedPairs);
        vo.setRawOcr(rawOcr);
        return vo;
    }

    private boolean hasBlankDetailedField(Map<String, String> detailedPairs) {
        for (String key : OcrExtractFieldConstants.DETAILED_KEYS) {
            if (!StringUtils.hasText(safeGet(detailedPairs, key))) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> mergeDetailedPairs(Map<String, String> rulePairs, Map<String, String> llmPairs) {
        Map<String, String> merged = OcrExtractFieldConstants.newDetailedMap();
        for (String key : OcrExtractFieldConstants.DETAILED_KEYS) {
            String ruleValue = safeGet(rulePairs, key);
            if (StringUtils.hasText(ruleValue)) {
                merged.put(key, ruleValue);
            } else {
                merged.put(key, safeGet(llmPairs, key));
            }
        }
        return merged;
    }

    private Map<String, String> mapDetailedToLegacyPairs(Map<String, String> detailedPairs) {
        Map<String, String> legacyPairs = OcrExtractFieldConstants.newLegacyMap();
        legacyPairs.put(OcrExtractFieldConstants.LEGACY_DEVICE_NO,
                safeGet(detailedPairs, OcrExtractFieldConstants.METER_POINT_ID));
        legacyPairs.put(OcrExtractFieldConstants.LEGACY_TEST_DATE,
                safeGet(detailedPairs, OcrExtractFieldConstants.TEST_DATE));
        legacyPairs.put(OcrExtractFieldConstants.LEGACY_A_DROP,
                safeGet(detailedPairs, OcrExtractFieldConstants.AO_DU));
        legacyPairs.put(OcrExtractFieldConstants.LEGACY_B_DROP,
                safeGet(detailedPairs, OcrExtractFieldConstants.BO_DU));
        legacyPairs.put(OcrExtractFieldConstants.LEGACY_C_DROP,
                safeGet(detailedPairs, OcrExtractFieldConstants.CO_DU));
        return legacyPairs;
    }

    private String safeGet(Map<String, String> pairs, String key) {
        if (pairs == null) {
            return "";
        }
        String value = pairs.get(key);
        return value == null ? "" : value;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传图片文件");
        }
        if (file.getSize() > maxImageSize) {
            throw new IllegalArgumentException("图片大小不能超过 " + (maxImageSize / 1024 / 1024) + "MB");
        }

        String filename = file.getOriginalFilename();
        String extension = getExtension(filename);
        if (!StringUtils.hasText(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持 jpg/jpeg/png 格式图片");
        }

        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType)) {
            String normalized = contentType.toLowerCase(Locale.ROOT).trim();
            if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
                throw new IllegalArgumentException("仅支持 jpg/jpeg/png 格式图片");
            }
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx >= filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}

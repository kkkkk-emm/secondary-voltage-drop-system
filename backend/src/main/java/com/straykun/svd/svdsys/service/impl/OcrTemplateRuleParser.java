package com.straykun.svd.svdsys.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR 模板规则解析器。
 */
@Component
public class OcrTemplateRuleParser {

    private static final Pattern VOLTAGE_PATTERN = Pattern.compile(
            "(?:一次电压|二次电压)\\s*[:：]?\\s*([-+]?\\d+(?:\\.\\d+)?\\s*[A-Za-zVvUu%％]*)");
    private static final Pattern TAN_PATTERN = Pattern.compile(
            "tan\\s*[φΦpP]?\\s*[:：]?\\s*([-+]?\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern METER_POINT_PATTERN = Pattern.compile(
            "计量点编号\\s*[:：]?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern TEST_DATE_PATTERN = Pattern.compile(
            "(?:测试日期|试验日期)\\s*[:：]?\\s*([0-9]{8}|[0-9]{4}[-/][0-9]{1,2}[-/][0-9]{1,2})");
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile(
            "温\\s*度\\s*[:：]?\\s*([-+]?\\d+(?:\\.\\d+)?\\s*℃?)");
    private static final Pattern HUMIDITY_PATTERN = Pattern.compile(
            "湿\\s*度\\s*[:：]?\\s*([-+]?\\d+(?:\\.\\d+)?\\s*%?)");
    private static final Pattern R_PERCENT_PATTERN = Pattern.compile(
            "r\\s*%\\s*[:：]?\\s*([-+]?\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

    /**
     * 按模板规则从 OCR 文本中提取固定明细字段。
     *
     * @param ocrText OCR 拼接文本。
     * @return 返回明细字段键值对映射。
     */
    public Map<String, String> extractDetailedPairs(String ocrText) {
        Map<String, String> pairs = OcrExtractFieldConstants.newDetailedMap();
        if (!StringUtils.hasText(ocrText)) {
            return pairs;
        }

        // 提取固定单值字段
        String text = ocrText.replace("\r\n", "\n");
        fillByRegex(pairs, OcrExtractFieldConstants.PRIMARY_VOLTAGE, VOLTAGE_PATTERN, text); // 电压
        fillByRegex(pairs, OcrExtractFieldConstants.TAN_PHI, TAN_PATTERN, text); // tanφ
        fillByRegex(pairs, OcrExtractFieldConstants.METER_POINT_ID, METER_POINT_PATTERN, text); // 计量点编号
        fillByRegex(pairs, OcrExtractFieldConstants.TEST_DATE, TEST_DATE_PATTERN, text); // 日期
        fillByRegex(pairs, OcrExtractFieldConstants.TEMPERATURE, TEMPERATURE_PATTERN, text); // 温度
        fillByRegex(pairs, OcrExtractFieldConstants.HUMIDITY, HUMIDITY_PATTERN, text); // 湿度
        fillByRegex(pairs, OcrExtractFieldConstants.R_PERCENT, R_PERCENT_PATTERN, text); // r%

        // 按行标签提取 AO/BO/CO（三值）
        List<String> lines = splitLines(text);
        fillPhaseRow(pairs, lines, RowLabel.F,
                OcrExtractFieldConstants.AO_F, OcrExtractFieldConstants.BO_F, OcrExtractFieldConstants.CO_F);
        fillPhaseRow(pairs, lines, RowLabel.D,
                OcrExtractFieldConstants.AO_D, OcrExtractFieldConstants.BO_D, OcrExtractFieldConstants.CO_D);
        fillPhaseRow(pairs, lines, RowLabel.DU,
                OcrExtractFieldConstants.AO_DU, OcrExtractFieldConstants.BO_DU, OcrExtractFieldConstants.CO_DU);
        fillPhaseRow(pairs, lines, RowLabel.UPT,
                OcrExtractFieldConstants.AO_UPT_U, OcrExtractFieldConstants.BO_UPT_U, OcrExtractFieldConstants.CO_UPT_U);
        fillPhaseRow(pairs, lines, RowLabel.UYB,
                OcrExtractFieldConstants.AO_UYB_U, OcrExtractFieldConstants.BO_UYB_U, OcrExtractFieldConstants.CO_UYB_U);

        return pairs;
    }

    private void fillByRegex(Map<String, String> pairs, String key, Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            pairs.put(key, safeTrim(matcher.group(1)));
        }
    }

    private List<String> splitLines(String text) {
        String[] rawLines = text.split("\\n");
        List<String> lines = new ArrayList<>();
        for (String raw : rawLines) {
            String line = raw == null ? "" : raw.trim();
            if (StringUtils.hasText(line)) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void fillPhaseRow(Map<String, String> pairs, List<String> lines, RowLabel rowLabel,
                              String aoKey, String boKey, String coKey) {
        int rowIndex = findRowLabelIndex(lines, rowLabel);
        if (rowIndex < 0) {
            return;
        }

        List<String> values = collectRowValues(lines, rowIndex);
        if (!values.isEmpty()) {
            pairs.put(aoKey, values.get(0));
        }
        if (values.size() > 1) {
            pairs.put(boKey, values.get(1));
        }
        if (values.size() > 2) {
            pairs.put(coKey, values.get(2));
        }
    }

    private int findRowLabelIndex(List<String> lines, RowLabel label) {
        for (int i = 0; i < lines.size(); i++) {
            if (isRowLabel(lines.get(i), label)) {
                return i;
            }
        }
        return -1;
    }

    private List<String> collectRowValues(List<String> lines, int rowIndex) {
        List<String> values = new ArrayList<>();
        for (int i = rowIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String normalized = normalizeLine(line);
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            if (isAnyRowLabel(normalized)) {
                break;
            }
            if (isStopLine(normalized)) {
                break;
            }
            if (isNoiseLine(normalized)) {
                continue;
            }

            List<String> numbers = extractNumbers(line);
            for (String number : numbers) {
                values.add(number);
                if (values.size() >= 3) {
                    return values;
                }
            }
        }
        return values;
    }

    private boolean isRowLabel(String line, RowLabel label) {
        String normalized = normalizeLine(line);
        return switch (label) {
            case F -> isFLabel(normalized);
            case D -> isDLabel(normalized);
            case DU -> isDuLabel(normalized);
            case UPT -> isUptLabel(normalized);
            case UYB -> isUybLabel(normalized);
        };
    }

    private boolean isAnyRowLabel(String normalizedLine) {
        return isFLabel(normalizedLine)
                || isDLabel(normalizedLine)
                || isDuLabel(normalizedLine)
                || isUptLabel(normalizedLine)
                || isUybLabel(normalizedLine);
    }

    private boolean isFLabel(String normalizedLine) {
        return normalizedLine.startsWith("f");
    }

    private boolean isDLabel(String normalizedLine) {
        return normalizedLine.startsWith("d") && !normalizedLine.startsWith("du");
    }

    private boolean isDuLabel(String normalizedLine) {
        return normalizedLine.startsWith("du");
    }

    private boolean isUptLabel(String normalizedLine) {
        return normalizedLine.contains("upt");
    }

    private boolean isUybLabel(String normalizedLine) {
        return normalizedLine.contains("uyb");
    }

    private boolean isNoiseLine(String normalizedLine) {
        return "ao".equals(normalizedLine)
                || "bo".equals(normalizedLine)
                || "co".equals(normalizedLine)
                || normalizedLine.contains("pt侧")
                || normalizedLine.contains("不带自校")
                || normalizedLine.contains("按1测量")
                || normalizedLine.contains("按2储存")
                || normalizedLine.contains("按1")
                || normalizedLine.contains("按2");
    }

    private boolean isStopLine(String normalizedLine) {
        return normalizedLine.startsWith("r%") || normalizedLine.contains("测量结束");
    }

    private List<String> extractNumbers(String line) {
        List<String> numbers = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(line);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    private String normalizeLine(String line) {
        if (!StringUtils.hasText(line)) {
            return "";
        }
        return line.toLowerCase(Locale.ROOT)
                .replace("：", ":")
                .replace("（", "(")
                .replace("）", ")")
                .replace("％", "%")
                .replaceAll("\\s+", "");
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private enum RowLabel {
        F, D, DU, UPT, UYB
    }
}

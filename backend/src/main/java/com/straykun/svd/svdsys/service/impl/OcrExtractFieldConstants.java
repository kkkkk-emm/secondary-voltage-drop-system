package com.straykun.svd.svdsys.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OCR 字段常量定义。
 */
public final class OcrExtractFieldConstants {

    public static final String PRIMARY_VOLTAGE = "一次电压";
    public static final String TAN_PHI = "tanφ";
    public static final String METER_POINT_ID = "计量点编号";
    public static final String TEMPERATURE = "温度";
    public static final String TEST_DATE = "测试日期";
    public static final String HUMIDITY = "湿度";
    public static final String AO_F = "AO相别f";
    public static final String BO_F = "BO相别f";
    public static final String CO_F = "CO相别f";
    public static final String AO_D = "AO相别d";
    public static final String BO_D = "BO相别d";
    public static final String CO_D = "CO相别d";
    public static final String AO_DU = "AO相别dU";
    public static final String BO_DU = "BO相别dU";
    public static final String CO_DU = "CO相别dU";
    public static final String AO_UPT_U = "AO相别Upt:U";
    public static final String BO_UPT_U = "BO相别Upt:U";
    public static final String CO_UPT_U = "CO相别Upt:U";
    public static final String AO_UYB_U = "AO相别Uyb:U";
    public static final String BO_UYB_U = "BO相别Uyb:U";
    public static final String CO_UYB_U = "CO相别Uyb:U";
    public static final String R_PERCENT = "r%";

    public static final String LEGACY_TEST_NO = "试验编号";
    public static final String LEGACY_DEVICE_NO = "设备编号";
    public static final String LEGACY_TEST_DATE = "试验日期";
    public static final String LEGACY_A_DROP = "A相压降";
    public static final String LEGACY_B_DROP = "B相压降";
    public static final String LEGACY_C_DROP = "C相压降";
    public static final String LEGACY_CONCLUSION = "结论";

    public static final String[] DETAILED_KEYS = {
            PRIMARY_VOLTAGE, TAN_PHI, METER_POINT_ID, TEMPERATURE, TEST_DATE, HUMIDITY,
            AO_F, BO_F, CO_F,
            AO_D, BO_D, CO_D,
            AO_DU, BO_DU, CO_DU,
            AO_UPT_U, BO_UPT_U, CO_UPT_U,
            AO_UYB_U, BO_UYB_U, CO_UYB_U,
            R_PERCENT
    };

    public static final String[] LEGACY_KEYS = {
            LEGACY_TEST_NO, LEGACY_DEVICE_NO, LEGACY_TEST_DATE,
            LEGACY_A_DROP, LEGACY_B_DROP, LEGACY_C_DROP, LEGACY_CONCLUSION
    };

    private OcrExtractFieldConstants() {
    }

    /**
     * 创建并初始化明细字段键值对模板。
     *
     * @return 返回明细字段键值对映射。
     */
    public static Map<String, String> newDetailedMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : DETAILED_KEYS) {
            map.put(key, "");
        }
        return map;
    }

    /**
     * 创建并初始化兼容字段键值对模板。
     *
     * @return 返回兼容字段键值对映射。
     */
    public static Map<String, String> newLegacyMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : LEGACY_KEYS) {
            map.put(key, "");
        }
        return map;
    }
}

package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 标准分组响应视图对象。
 */
@Data
public class StandardGroupVO {

    /**
     * projectType 字段。
     */
    private String projectType;

    /**
     * gearLevel 字段。
     */
    private String gearLevel;

    /**
     * loadPercent 字段。
     */
    private String loadPercent;

    /**
     * groupKey 字段。
     */
    private String groupKey;

    /**
     * isPT 字段。
     */
    private Boolean isPT;

    /**
     * thresholds 字段。
     */
    private Map<String, LimitRange> thresholds;

    /**
     * 限制Range响应视图对象。
     */
    @Data
    public static class LimitRange {
        /**
         * min 字段。
         */
        private BigDecimal min;
        /**
         * max 字段。
         */
        private BigDecimal max;

        /**
         * 构造函数，初始化 LimitRange 所需依赖。
         *
         * @param min 参数 min。
         * @param max 参数 max。
         */
        public LimitRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}

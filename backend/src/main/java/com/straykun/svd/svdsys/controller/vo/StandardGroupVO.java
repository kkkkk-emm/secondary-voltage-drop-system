package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 标准组合VO（去重后的唯一组合）
 */
@Data
public class StandardGroupVO {

    /**
     * 项目类型：PT1/PT2/CT1/CT2
     */
    private String projectType;

    /**
     * 档位：100V/5A
     */
    private String gearLevel;

    /**
     * 负载百分比：100%
     */
    private String loadPercent;

    /**
     * 组合标识（用于前端选择）：projectType-gearLevel-loadPercent
     */
    private String groupKey;

    /**
     * 是否为PT项目（PT项目有5项数据，CT项目只有2项）
     */
    private Boolean isPT;

    /**
     * 各阈值项的范围 Map<thresholdItem, LimitRange>
     * 例如: {"f": {min: -0.05, max: 0.05}, "delta": {min: -2, max: 2}, ...}
     */
    private Map<String, LimitRange> thresholds;

    @Data
    public static class LimitRange {
        private BigDecimal min;
        private BigDecimal max;

        public LimitRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}

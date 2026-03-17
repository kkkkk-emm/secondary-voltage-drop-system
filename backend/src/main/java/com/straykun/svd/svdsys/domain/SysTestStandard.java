package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 系统检定标准实体对象。
 */
@Data
public class SysTestStandard {

    /**
     * 主键 ID。
     */
    private Long id;

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
     * limitMin 字段。
     */
    private BigDecimal limitMin;

    /**
     * limitMax 字段。
     */
    private BigDecimal limitMax;

    /**
     * thresholdItem 字段。
     */
    private String thresholdItem;
}

package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 标准响应视图对象。
 */
@Data
public class StandardVO {

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

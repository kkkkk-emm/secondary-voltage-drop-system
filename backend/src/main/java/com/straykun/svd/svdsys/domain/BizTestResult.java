package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 业务检定结果实体对象。
 */
@Data
public class BizTestResult {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * taskId 字段。
     */
    private Long taskId;

    /**
     * standardId 字段。
     */
    private Long standardId;

    /**
     * 相别。
     */
    private String phase;

    /**
     * f(%) 测量值。
     */
    private BigDecimal valF;

    /**
     * δ(分) 测量值。
     */
    private BigDecimal valDelta;

    /**
     * dU(%) 测量值。
     */
    private BigDecimal valDu;

    /**
     * Upt 测量值。
     */
    private BigDecimal valUpt;

    /**
     * Uyb 测量值。
     */
    private BigDecimal valUyb;

    /**
     * 是否合格标记。
     */
    private Integer isPass;
}

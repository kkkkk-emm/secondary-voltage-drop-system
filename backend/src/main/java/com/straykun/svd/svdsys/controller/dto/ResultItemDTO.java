package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结果明细项请求参数对象。
 */
@Data
public class ResultItemDTO {

    /**
     * 相别。
     */
    @NotNull(message = "相别不能为空")
    private String phase;

    // 标准组合字段（替代 standardId）
    /**
     * projectType 字段。
     */
    @NotNull(message = "项目类型不能为空")
    private String projectType;

    /**
     * gearLevel 字段。
     */
    @NotNull(message = "电压挡位不能为空")
    private String gearLevel;

    /**
     * loadPercent 字段。
     */
    @NotNull(message = "负载百分比不能为空")
    private String loadPercent;

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
}

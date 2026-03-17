package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结果修正请求参数对象。
 */
@Data
public class ResultCorrectRequest {

    /**
     * resultId 字段。
     */
    @NotNull(message = "检测结果ID不能为空")
    private Long resultId;

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
     * reason 字段。
     */
    private String reason;
}

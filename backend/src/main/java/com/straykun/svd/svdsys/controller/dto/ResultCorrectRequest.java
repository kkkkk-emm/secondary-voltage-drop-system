package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据修正请求 DTO
 */
@Data
public class ResultCorrectRequest {

    @NotNull(message = "检测结果ID不能为空")
    private Long resultId;

    private BigDecimal valF;

    private BigDecimal valDelta;

    private BigDecimal valDu;

    private BigDecimal valUpt;

    private BigDecimal valUyb;

    /**
     * 修正原因
     */
    private String reason;
}

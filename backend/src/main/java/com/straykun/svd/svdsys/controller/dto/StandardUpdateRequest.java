package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 标准更新请求参数对象。
 */
@Data
public class StandardUpdateRequest {

    /**
     * 主键 ID。
     */
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * limitMin 字段。
     */
    @NotNull(message = "下限不能为空")
    private BigDecimal limitMin;

    /**
     * limitMax 字段。
     */
    @NotNull(message = "上限不能为空")
    private BigDecimal limitMax;
}

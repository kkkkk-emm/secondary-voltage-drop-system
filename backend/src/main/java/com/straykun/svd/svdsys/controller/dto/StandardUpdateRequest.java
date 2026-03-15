package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StandardUpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "下限不能为空")
    private BigDecimal limitMin;

    @NotNull(message = "上限不能为空")
    private BigDecimal limitMax;
}



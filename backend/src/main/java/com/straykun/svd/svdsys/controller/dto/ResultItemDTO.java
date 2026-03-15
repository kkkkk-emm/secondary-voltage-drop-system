package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultItemDTO {

    @NotNull(message = "相别不能为空")
    private String phase;

    // 标准组合字段（替代 standardId）
    @NotNull(message = "项目类型不能为空")
    private String projectType;

    @NotNull(message = "电压挡位不能为空")
    private String gearLevel;

    @NotNull(message = "负载百分比不能为空")
    private String loadPercent;

    private BigDecimal valF;

    private BigDecimal valDelta;

    private BigDecimal valDu;

    private BigDecimal valUpt;

    private BigDecimal valUyb;
}



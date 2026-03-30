package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结果明细项请求参数对象。
 */
@Data
public class ResultItemDTO {

    @NotBlank(message = "相别不能为空")
    private String phase;

    @NotBlank(message = "项目类型不能为空")
    private String projectType;

    @NotBlank(message = "电压挡位不能为空")
    private String gearLevel;

    @NotBlank(message = "负载百分比不能为空")
    private String loadPercent;

    private BigDecimal valF;

    private BigDecimal valDelta;

    private BigDecimal valDu;

    private BigDecimal valUpt;

    private BigDecimal valUyb;
}

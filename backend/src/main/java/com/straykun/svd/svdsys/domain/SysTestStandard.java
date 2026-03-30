package com.straykun.svd.svdsys.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 系统检定标准实体对象。
 */
@Data
public class SysTestStandard {

    @NotNull(message = "标准ID不能为空")
    private Long id;

    private String projectType;

    private String gearLevel;

    private String loadPercent;

    @NotNull(message = "下限不能为空")
    private BigDecimal limitMin;

    @NotNull(message = "上限不能为空")
    private BigDecimal limitMax;

    private String thresholdItem;
}

package com.straykun.svd.svdsys.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 业务检定结果实体对象。
 */
@Data
public class BizTestResult {

    private Long id;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "标准ID不能为空")
    private Long standardId;

    @NotBlank(message = "相别不能为空")
    private String phase;

    private BigDecimal valF;

    private BigDecimal valDelta;

    private BigDecimal valDu;

    private BigDecimal valUpt;

    private BigDecimal valUyb;

    @NotNull(message = "合格标记不能为空")
    private Integer isPass;
}

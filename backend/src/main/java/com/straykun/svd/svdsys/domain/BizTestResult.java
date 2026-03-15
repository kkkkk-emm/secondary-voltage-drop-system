package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 检测结果明细表 biz_test_result
 */
@Data
public class BizTestResult {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 标准ID
     */
    private Long standardId;

    /**
     * 相位(A/B/C)
     */
    private String phase;

    /**
     * 比差 f(%)
     */
    private BigDecimal valF;

    /**
     * 角差 δ(')
     */
    private BigDecimal valDelta;

    /**
     * 压降 ΔU(%)
     */
    private BigDecimal valDu;

    /**
     * PT二次电压 Upt(V)
     */
    private BigDecimal valUpt;

    /**
     * 仪表端电压 Uyb(V)
     */
    private BigDecimal valUyb;

    /**
     * 是否合格 1:合格 0:不合格
     */
    private Integer isPass;
}

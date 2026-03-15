package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 标准阈值配置表 sys_test_standard
 */
@Data
public class SysTestStandard {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 项目(PT1/PT2/CT1/CT2)
     */
    private String projectType;

    /**
     * 档位(100V/5A)
     */
    private String gearLevel;

    /**
     * 负载百分比 (20%/100%)
     */
    private String loadPercent;

    /**
     * 数据下限
     */
    private BigDecimal limitMin;

    /**
     * 数据上限
     */
    private BigDecimal limitMax;

    /**
     * 阈值项目
     */
    private String thresholdItem;
}

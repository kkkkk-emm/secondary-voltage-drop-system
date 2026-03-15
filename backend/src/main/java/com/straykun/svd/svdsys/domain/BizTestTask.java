package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检测任务主表 biz_test_task
 */
@Data
public class BizTestTask {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 计量点编号
     */
    private String meterPointId;

    /**
     * 送检日期
     */
    private LocalDate deliverDate;

    /**
     * 检测时间
     */
    private LocalDateTime testDate;

    /**
     * 温度(℃)
     */
    private BigDecimal temperature;

    /**
     * 湿度(%)
     */
    private BigDecimal humidity;

    /**
     * tanφ
     */
    private BigDecimal tanPhi;

    /**
     * R(%)
     */
    private BigDecimal rPercent;
}

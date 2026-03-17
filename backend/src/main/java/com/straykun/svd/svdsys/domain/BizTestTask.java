package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业务检定检定任务实体对象。
 */
@Data
public class BizTestTask {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 设备 ID。
     */
    private Long deviceId;

    /**
     * 操作员 ID。
     */
    private Long operatorId;

    /**
     * 计量点编号。
     */
    private String meterPointId;

    /**
     * 送检日期，格式为 yyyy-MM-dd。
     */
    private LocalDate deliverDate;

    /**
     * 测试日期，格式为 yyyy-MM-dd HH:mm:ss。
     */
    private LocalDateTime testDate;

    /**
     * 环境温度。
     */
    private BigDecimal temperature;

    /**
     * 环境湿度。
     */
    private BigDecimal humidity;

    /**
     * tanφ 值。
     */
    private BigDecimal tanPhi;

    /**
     * r% 值。
     */
    private BigDecimal rPercent;
}

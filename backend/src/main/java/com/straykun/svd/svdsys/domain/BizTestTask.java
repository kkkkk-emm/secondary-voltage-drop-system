package com.straykun.svd.svdsys.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业务检定任务实体对象。
 */
@Data
public class BizTestTask {

    private Long id;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "操作员ID不能为空")
    private Long operatorId;

    @NotBlank(message = "计量点编号不能为空")
    private String meterPointId;

    @NotNull(message = "送检日期不能为空")
    private LocalDate deliverDate;

    @NotNull(message = "测试日期不能为空")
    private LocalDateTime testDate;

    @NotNull(message = "环境温度不能为空")
    private BigDecimal temperature;

    @NotNull(message = "环境湿度不能为空")
    private BigDecimal humidity;

    @NotNull(message = "tanφ不能为空")
    private BigDecimal tanPhi;

    @NotNull(message = "r%不能为空")
    private BigDecimal rPercent;
}

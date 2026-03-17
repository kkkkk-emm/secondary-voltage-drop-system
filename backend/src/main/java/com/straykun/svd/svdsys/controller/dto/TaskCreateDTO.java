package com.straykun.svd.svdsys.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 检定任务创建请求参数对象。
 */
@Data
public class TaskCreateDTO {

    /**
     * 设备 ID。
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 计量点编号。
     */
    @NotNull(message = "计量点编号不能为空")
    private String meterPointId;

    /**
     * 送检日期，格式为 yyyy-MM-dd。
     */
    @NotNull(message = "送检日期不能为空")
    private String deliverDate; // yyyy-MM-dd

    /**
     * 测试日期，格式为 yyyy-MM-dd HH:mm:ss。
     */
    @NotNull(message = "测试日期不能为空")
    private String testDate; // yyyy-MM-dd HH:mm:ss

    /**
     * 环境温度。
     */
    @NotNull(message = "环境温度不能为空")
    private BigDecimal temperature;

    /**
     * 环境湿度。
     */
    @NotNull(message = "环境湿度不能为空")
    private BigDecimal humidity;

    /**
     * tanφ 值。
     */
    @NotNull(message = "tanφ不能为空")
    @JsonProperty("tanPhi")
    private BigDecimal tanPhi;

    /**
     * r% 值。
     */
    @NotNull(message = "r%不能为空")
    @JsonProperty("rPercent")
    private BigDecimal rPercent;

    /**
     * 明细结果列表。
     */
    @NotEmpty(message = "明细结果不能为空")
    @Valid
    private List<ResultItemDTO> resultList;
}

package com.straykun.svd.svdsys.controller.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 检定任务详情响应视图对象。
 */
@Data
public class TaskDetailVO {

    /**
     * 检定任务Info响应视图对象。
     */
    @Data
    public static class TaskInfo {
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
        private String deliverDate;
        /**
         * 测试日期，格式为 yyyy-MM-dd HH:mm:ss。
         */
        private String testDate;
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
        @JsonProperty("tanPhi")
        private BigDecimal tanPhi;
        /**
         * r% 值。
         */
        @JsonProperty("rPercent")
        private BigDecimal rPercent;
    }

    /**
     * 设备Info响应视图对象。
     */
    @Data
    public static class DeviceInfo {
        /**
         * 产品编号。
         */
        private String productNo;
        /**
         * 产品名称。
         */
        private String productName;
        /**
         * 型号。
         */
        private String model;
        /**
         * 制造商。
         */
        private String manufacturer;
    }

    /**
     * 结果Info响应视图对象。
     */
    @Data
    public static class ResultInfo {
        /**
         * 主键 ID。
         */
        private Long id;
        /**
         * 相别。
         */
        private String phase;
        /**
         * f(%) 测量值。
         */
        private java.math.BigDecimal valF;
        /**
         * δ(分) 测量值。
         */
        private java.math.BigDecimal valDelta;
        /**
         * dU(%) 测量值。
         */
        private java.math.BigDecimal valDu;
        /**
         * Upt 测量值。
         */
        private java.math.BigDecimal valUpt;
        /**
         * Uyb 测量值。
         */
        private java.math.BigDecimal valUyb;
        /**
         * 是否合格标记。
         */
        private Integer isPass;
    }

    /**
     * 任务基础信息。
     */
    private TaskInfo taskInfo;

    /**
     * 设备信息。
     */
    private DeviceInfo deviceInfo;

    /**
     * 操作员姓名。
     */
    private String operatorName;

    /**
     * 明细结果列表。
     */
    private List<ResultInfo> resultList;
}

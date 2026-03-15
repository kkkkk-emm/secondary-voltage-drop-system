package com.straykun.svd.svdsys.controller.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TaskDetailVO {

    @Data
    public static class TaskInfo {
        private Long id;
        private Long deviceId;
        private Long operatorId;
        private String meterPointId;
        private String deliverDate;
        private String testDate;
        private BigDecimal temperature;
        private BigDecimal humidity;
        @JsonProperty("tanPhi")
        private BigDecimal tanPhi;
        @JsonProperty("rPercent")
        private BigDecimal rPercent;
    }

    @Data
    public static class DeviceInfo {
        private String productNo;
        private String productName;
        private String model;
        private String manufacturer;
    }

    @Data
    public static class ResultInfo {
        private Long id;
        private String phase;
        private java.math.BigDecimal valF;
        private java.math.BigDecimal valDelta;
        private java.math.BigDecimal valDu;
        private java.math.BigDecimal valUpt;
        private java.math.BigDecimal valUyb;
        private Integer isPass;
    }

    private TaskInfo taskInfo;

    private DeviceInfo deviceInfo;

    private String operatorName;

    private List<ResultInfo> resultList;
}



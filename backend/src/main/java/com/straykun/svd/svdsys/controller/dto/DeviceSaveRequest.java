package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceSaveRequest {

    @NotBlank(message = "产品编号不能为空")
    private String productNo;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String model;

    private String manufacturer;

    private String placeOrigin;

    /**
     * 生产日期 yyyy-MM-dd
     */
    private String productionDate;
}



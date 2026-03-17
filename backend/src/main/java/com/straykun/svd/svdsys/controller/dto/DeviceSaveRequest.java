package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备新增请求参数对象。
 */
@Data
public class DeviceSaveRequest {

    /**
     * 产品编号。
     */
    @NotBlank(message = "产品编号不能为空")
    private String productNo;

    /**
     * 产品名称。
     */
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    /**
     * 型号。
     */
    private String model;

    /**
     * 制造商。
     */
    private String manufacturer;

    /**
     * placeOrigin 字段。
     */
    private String placeOrigin;

    /**
     * productionDate 字段。
     */
    private String productionDate;
}

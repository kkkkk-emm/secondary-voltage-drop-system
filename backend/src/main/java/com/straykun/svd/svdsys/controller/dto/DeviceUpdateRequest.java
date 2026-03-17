package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备更新请求参数对象。
 */
@Data
public class DeviceUpdateRequest {

    /**
     * 主键 ID。
     */
    @NotNull(message = "设备ID不能为空")
    private Long id;

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

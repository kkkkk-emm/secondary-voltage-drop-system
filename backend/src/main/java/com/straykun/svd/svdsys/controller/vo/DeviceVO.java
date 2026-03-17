package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

/**
 * 设备响应视图对象。
 */
@Data
public class DeviceVO {

    /**
     * 主键 ID。
     */
    private Long id;

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

    /**
     * placeOrigin 字段。
     */
    private String placeOrigin;

    /**
     * productionDate 字段。
     */
    private String productionDate;

    /**
     * createTime 字段。
     */
    private String createTime;
}

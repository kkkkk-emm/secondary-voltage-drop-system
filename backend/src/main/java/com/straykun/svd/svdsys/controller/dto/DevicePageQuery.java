package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

/**
 * 设备分页请求参数对象。
 */
@Data
public class DevicePageQuery {

    /**
     * page 字段。
     */
    private Integer page = 1;

    /**
     * 每页大小。
     */
    private Integer size = 10;

    /**
     * 产品编号。
     */
    private String productNo;

    /**
     * 产品名称。
     */
    private String productName;

    /**
     * 制造商。
     */
    private String manufacturer;
}

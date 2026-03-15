package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

@Data
public class DevicePageQuery {

    /**
     * 当前页（默认1）
     */
    private Integer page = 1;

    /**
     * 每页条数（默认10）
     */
    private Integer size = 10;

    /**
     * 产品编号（精确查询）
     */
    private String productNo;

    /**
     * 产品名称（模糊查询）
     */
    private String productName;

    /**
     * 制造商（模糊查询）
     */
    private String manufacturer;
}



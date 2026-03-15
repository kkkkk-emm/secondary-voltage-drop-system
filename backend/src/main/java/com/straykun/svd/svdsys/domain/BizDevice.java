package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 被检查设备档案表 biz_device
 */
@Data
public class BizDevice {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 产品编号
     */
    private String productNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 规格型号
     */
    private String model;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 产地
     */
    private String placeOrigin;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

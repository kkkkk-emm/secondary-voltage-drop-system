package com.straykun.svd.svdsys.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业务设备实体对象。
 */
@Data
public class BizDevice {

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
    private LocalDate productionDate;

    /**
     * createTime 字段。
     */
    private LocalDateTime createTime;
}

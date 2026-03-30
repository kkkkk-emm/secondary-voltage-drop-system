package com.straykun.svd.svdsys.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业务设备实体对象。
 */
@Data
public class BizDevice {

    private Long id;

    @NotBlank(message = "产品编号不能为空")
    private String productNo;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String model;

    private String manufacturer;

    private String placeOrigin;

    private LocalDate productionDate;

    private LocalDateTime createTime;
}

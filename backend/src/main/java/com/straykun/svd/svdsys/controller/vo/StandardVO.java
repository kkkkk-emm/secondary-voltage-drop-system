package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StandardVO {

    private Long id;

    private String projectType;

    private String gearLevel;

    private String loadPercent;

    private BigDecimal limitMin;

    private BigDecimal limitMax;

    private String thresholdItem;
}



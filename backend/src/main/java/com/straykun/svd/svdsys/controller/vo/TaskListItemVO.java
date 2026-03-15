package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

@Data
public class TaskListItemVO {

    private Long id;

    private Long deviceId;

    private String deviceProductNo;

    private String deviceProductName;

    private Long operatorId;

    private String operatorName;

    private String meterPointId;

    private String testDate;

    /**
     * 最终结论 (1:合格, 0:不合格)
     */
    private Integer result;
}



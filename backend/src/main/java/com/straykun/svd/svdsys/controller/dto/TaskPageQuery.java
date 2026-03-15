package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

@Data
public class TaskPageQuery {

    private Integer page = 1;

    private Integer size = 10;

    /**
     * 任务ID（精确查询）
     */
    private Long taskId;

    /**
     * 设备编号（模糊查询）
     */
    private String deviceProductNo;

    /**
     * 检定员姓名（模糊查询）
     */
    private String operatorName;

    private String startDate; // yyyy-MM-dd

    private String endDate; // yyyy-MM-dd

    /**
     * 0:不合格 1:合格
     */
    private Integer isPass;
}



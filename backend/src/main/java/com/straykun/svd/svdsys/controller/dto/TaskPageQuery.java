package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

/**
 * 检定任务分页请求参数对象。
 */
@Data
public class TaskPageQuery {

    /**
     * page 字段。
     */
    private Integer page = 1;

    /**
     * 每页大小。
     */
    private Integer size = 10;

    /**
     * taskId 字段。
     */
    private Long taskId;

    /**
     * deviceProductNo 字段。
     */
    private String deviceProductNo;

    /**
     * 操作员姓名。
     */
    private String operatorName;

    /**
     * startDate 字段。
     */
    private String startDate; // yyyy-MM-dd

    /**
     * endDate 字段。
     */
    private String endDate; // yyyy-MM-dd

    /**
     * 是否合格标记。
     */
    private Integer isPass;
}

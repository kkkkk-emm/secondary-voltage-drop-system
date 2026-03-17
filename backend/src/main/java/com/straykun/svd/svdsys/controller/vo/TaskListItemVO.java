package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

/**
 * 检定任务列表明细项响应视图对象。
 */
@Data
public class TaskListItemVO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 设备 ID。
     */
    private Long deviceId;

    /**
     * deviceProductNo 字段。
     */
    private String deviceProductNo;

    /**
     * deviceProductName 字段。
     */
    private String deviceProductName;

    /**
     * 操作员 ID。
     */
    private Long operatorId;

    /**
     * 操作员姓名。
     */
    private String operatorName;

    /**
     * 计量点编号。
     */
    private String meterPointId;

    /**
     * 测试日期，格式为 yyyy-MM-dd HH:mm:ss。
     */
    private String testDate;

    /**
     * result 字段。
     */
    private Integer result;
}

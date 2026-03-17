package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志响应视图对象。
 */
@Data
public class LogVO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * operation 字段。
     */
    private String operation;

    /**
     * method 字段。
     */
    private String method;

    /**
     * params 字段。
     */
    private String params;

    /**
     * time 字段。
     */
    private Long time;

    /**
     * ip 字段。
     */
    private String ip;

    /**
     * createTime 字段。
     */
    private LocalDateTime createTime;
}

package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志返回对象
 */
@Data
public class LogVO {

    private Long id;

    private String username;

    private String operation;

    private String method;

    private String params;

    private Long time;

    private String ip;

    private LocalDateTime createTime;
}


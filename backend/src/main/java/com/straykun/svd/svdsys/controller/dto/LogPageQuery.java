package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

/**
 * 日志分页查询参数
 */
@Data
public class LogPageQuery {

    private Integer page = 1;

    private Integer size = 10;

    private String username;

    private String startDate;

    private String endDate;
}


package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

/**
 * 日志分页请求参数对象。
 */
@Data
public class LogPageQuery {

    /**
     * page 字段。
     */
    private Integer page = 1;

    /**
     * 每页大小。
     */
    private Integer size = 10;

    /**
     * 用户名。
     */
    private String username;

    /**
     * startDate 字段。
     */
    private String startDate;

    /**
     * endDate 字段。
     */
    private String endDate;
}

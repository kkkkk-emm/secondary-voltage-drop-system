package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

/**
 * 用户分页请求参数对象。
 */
@Data
public class UserPageQuery {

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
     * 角色编码。
     */
    private Integer role;

    /**
     * 状态编码。
     */
    private Integer status;
}

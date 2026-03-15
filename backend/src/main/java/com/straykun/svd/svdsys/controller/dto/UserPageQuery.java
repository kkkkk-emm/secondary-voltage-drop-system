package com.straykun.svd.svdsys.controller.dto;

import lombok.Data;

@Data
public class UserPageQuery {

    private Integer page = 1;

    private Integer size = 10;

    private String username;

    /**
     * 角色 (0:管理员, 1:检测员)
     */
    private Integer role;

    /**
     * 状态(1:启用, 0:禁用)
     */
    private Integer status;
}



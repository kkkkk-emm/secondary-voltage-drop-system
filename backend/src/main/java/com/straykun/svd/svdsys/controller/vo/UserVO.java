package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

/**
 * 用户响应视图对象。
 */
@Data
public class UserVO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 真实姓名。
     */
    private String realName;

    /**
     * 角色编码。
     */
    private Integer role;

    /**
     * 状态编码。
     */
    private Integer status;
}

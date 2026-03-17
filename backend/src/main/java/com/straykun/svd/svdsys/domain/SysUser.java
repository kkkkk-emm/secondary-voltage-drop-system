package com.straykun.svd.svdsys.domain;

import lombok.Data;

/**
 * 系统用户实体对象。
 */
@Data
public class SysUser {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 密码。
     */
    private String password;

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

package com.straykun.svd.svdsys.domain;

import lombok.Data;

/**
 * 系统用户表 sys_user
 */
@Data
public class SysUser {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色(0:管理员, 1:检测员)
     */
    private Integer role;

    /**
     * 状态(1:启用, 0:禁用)
     */
    private Integer status;
}

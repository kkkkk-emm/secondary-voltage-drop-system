package com.straykun.svd.svdsys.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 系统用户实体对象。
 */
@Data
public class SysUser {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "角色不能为空")
    private Integer role;

    @NotNull(message = "状态不能为空")
    private Integer status;
}

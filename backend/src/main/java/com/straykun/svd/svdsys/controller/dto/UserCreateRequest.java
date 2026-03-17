package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户创建请求参数对象。
 */
@Data
public class UserCreateRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 真实姓名。
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 角色编码。
     */
    @NotNull(message = "角色不能为空")
    private Integer role;
}

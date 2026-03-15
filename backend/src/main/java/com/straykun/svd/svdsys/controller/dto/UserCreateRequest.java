package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 角色 (0:管理员, 1:检测员)
     */
    @NotNull(message = "角色不能为空")
    private Integer role;
}



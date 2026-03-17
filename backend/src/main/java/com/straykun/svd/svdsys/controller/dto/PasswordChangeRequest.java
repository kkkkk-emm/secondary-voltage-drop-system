package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码修改请求参数对象。
 */
@Data
public class PasswordChangeRequest {

    /**
     * oldPassword 字段。
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * newPassword 字段。
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}

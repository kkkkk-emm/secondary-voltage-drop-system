package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户重置密码请求参数对象。
 */
@Data
public class UserResetPasswordRequest {

    /**
     * 主键 ID。
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;
}

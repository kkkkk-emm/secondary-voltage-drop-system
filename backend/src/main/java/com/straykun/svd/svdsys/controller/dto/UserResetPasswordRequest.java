package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserResetPasswordRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;
}



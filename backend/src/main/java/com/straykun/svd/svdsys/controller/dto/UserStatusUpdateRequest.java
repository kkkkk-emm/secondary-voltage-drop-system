package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 0:禁用, 1:启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}



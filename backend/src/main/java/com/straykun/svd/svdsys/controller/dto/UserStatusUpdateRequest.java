package com.straykun.svd.svdsys.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新请求参数对象。
 */
@Data
public class UserStatusUpdateRequest {

    /**
     * 主键 ID。
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 状态编码。
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

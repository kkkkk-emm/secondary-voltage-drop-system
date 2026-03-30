package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.PasswordChangeRequest;
import com.straykun.svd.svdsys.controller.dto.UserCreateRequest;
import com.straykun.svd.svdsys.controller.dto.UserPageQuery;
import com.straykun.svd.svdsys.controller.dto.UserResetPasswordRequest;
import com.straykun.svd.svdsys.controller.dto.UserStatusUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.UserVO;
import com.straykun.svd.svdsys.security.SecurityUtils;
import com.straykun.svd.svdsys.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserVO>> page(UserPageQuery query) {
        return Result.success(userService.page(query));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("新增用户")
    public Result<Void> create(@RequestBody @Valid UserCreateRequest request) {
        try {
            userService.create(request);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/password")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<Void> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            return Result.fail(401, "未认证");
        }
        try {
            userService.changePassword(Long.valueOf(userIdStr), request);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("修改用户状态")
    public Result<Void> updateStatus(@RequestBody @Valid UserStatusUpdateRequest request) {
        try {
            userService.updateStatus(request);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PutMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("重置用户密码")
    public Result<Void> resetPassword(@RequestBody @Valid UserResetPasswordRequest request) {
        try {
            userService.resetPassword(request);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}

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
import com.straykun.svd.svdsys.domain.SysUser;
import com.straykun.svd.svdsys.mapper.SysUserMapper;
import com.straykun.svd.svdsys.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

/**
 * 用户相关接口（基础能力）
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    @Value("${svd.user.default-password}")
    private String defaultPassword;

    public UserController(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户分页查询（仅管理员）
     *
     * @param query 查询条件
     * @return 用户列表分页结果
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserVO>> page(UserPageQuery query) {
        long current = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        long size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        long offset = (current - 1) * size;

        java.util.List<SysUser> list = sysUserMapper.selectPage(
                offset,
                size,
                query.getUsername(),
                query.getRole(),
                query.getStatus());
        long total = sysUserMapper.count(
                query.getUsername(),
                query.getRole(),
                query.getStatus());
        java.util.List<UserVO> records = new java.util.ArrayList<>();
        for (SysUser u : list) {
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setRealName(u.getRealName());
            vo.setRole(u.getRole());
            vo.setStatus(u.getStatus());
            records.add(vo);
        }
        return Result.success(PageResult.of(total, size, current, records));
    }

    /**
     * 新增用户（仅管理员）
     *
     * @param request 用户创建请求
     * @return 成功结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("新增用户")
    public Result<Void> create(@RequestBody @Valid UserCreateRequest request) {
        SysUser exists = sysUserMapper.selectByUsername(request.getUsername());
        if (exists != null) {
            return Result.fail(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setStatus(1);
        // 默认密码 Cug@2025，使用当前 PasswordEncoder 加密
        user.setPassword(passwordEncoder.encode(defaultPassword));
        sysUserMapper.insert(user);
        return Result.success();
    }

    /**
     * 当前登录用户修改密码
     *
     * @param request 密码修改请求
     * @return 成功结果
     */
    @PutMapping("/password")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<Void> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            return Result.fail(401, "未认证");
        }
        Long userId = Long.valueOf(userIdStr);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.fail(400, "用户不存在");
        }
        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.fail(400, "原密码不正确");
        }
        // 更新为新密码
        String encoded = passwordEncoder.encode(request.getNewPassword());
        sysUserMapper.updatePassword(userId, encoded);
        return Result.success();
    }

    /**
     * 修改用户状态（仅管理员）
     *
     * @param request 状态更新请求
     * @return 成功结果
     */
    @PutMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("修改用户状态")
    public Result<Void> updateStatus(@RequestBody @Valid UserStatusUpdateRequest request) {
        sysUserMapper.updateStatus(request.getId(), request.getStatus());
        return Result.success();
    }

    /**
     * 重置密码为默认值（仅管理员）
     *
     * @param request 重置密码请求
     * @return 成功结果
     */
    @PutMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("重置用户密码")
    public Result<Void> resetPassword(@RequestBody @Valid UserResetPasswordRequest request) {
        String encoded = passwordEncoder.encode(defaultPassword);
        sysUserMapper.updatePassword(request.getId(), encoded);
        return Result.success();
    }
}

package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.LoginRequest;
import com.straykun.svd.svdsys.domain.SysUser;
import com.straykun.svd.svdsys.mapper.SysUserMapper;
import com.straykun.svd.svdsys.security.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数，初始化 AuthController 所需依赖。
     *
     * @param sysUserMapper 参数 sysUserMapper。
     * @param jwtUtil 参数 jwtUtil。
     * @param passwordEncoder 参数 passwordEncoder。
     */
    public AuthController(SysUserMapper sysUserMapper,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 执行用户登录认证并返回 token 与用户信息。
     *
     * @param request 登录请求参数。
     * @return 返回统一响应结果。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest request) {
        try {
            SysUser user = sysUserMapper.selectByUsername(request.getUsername());
            if (user == null || user.getStatus() == null || user.getStatus() == 0) {
                return Result.fail(400, "用户不存在或已禁用");
            }
            // 使用 SHA-256 加密校验
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Result.fail(400, "用户名或密码错误");
            }

            String token = jwtUtil.generateToken(user);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            data.put("userInfo", userInfo);

            return Result.success(data);
        } catch (Exception e) {
            log.error("登录异常: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return Result.fail(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 执行退出登录处理。
     *
     * @return 返回统一响应结果。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

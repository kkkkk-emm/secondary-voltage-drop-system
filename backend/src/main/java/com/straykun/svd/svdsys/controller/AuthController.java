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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(SysUserMapper sysUserMapper,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果（包含Token和用户信息）
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
     * 用户登出
     *
     * @return 成功结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

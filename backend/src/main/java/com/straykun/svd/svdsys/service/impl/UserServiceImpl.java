package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.PasswordChangeRequest;
import com.straykun.svd.svdsys.controller.dto.UserCreateRequest;
import com.straykun.svd.svdsys.controller.dto.UserPageQuery;
import com.straykun.svd.svdsys.controller.dto.UserResetPasswordRequest;
import com.straykun.svd.svdsys.controller.dto.UserStatusUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.UserVO;
import com.straykun.svd.svdsys.domain.SysUser;
import com.straykun.svd.svdsys.mapper.SysUserMapper;
import com.straykun.svd.svdsys.service.UserService;
import com.straykun.svd.svdsys.service.support.EntityValidationSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User service implementation.
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EntityValidationSupport entityValidationSupport;

    @Value("${svd.user.default-password}")
    private String defaultPassword;

    public UserServiceImpl(SysUserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           EntityValidationSupport entityValidationSupport) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.entityValidationSupport = entityValidationSupport;
    }

    @Override
    public PageResult<UserVO> page(UserPageQuery query) {
        long current = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        long size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        long offset = (current - 1) * size;

        List<SysUser> list = userMapper.selectPage(
                offset,
                size,
                query.getUsername(),
                query.getRole(),
                query.getStatus());
        long total = userMapper.count(
                query.getUsername(),
                query.getRole(),
                query.getStatus());

        List<UserVO> records = new ArrayList<>();
        for (SysUser user : list) {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setRole(user.getRole());
            vo.setStatus(user.getStatus());
            records.add(vo);
        }
        return PageResult.of(total, size, current, records);
    }

    @Override
    public void create(UserCreateRequest request) {
        SysUser exists = userMapper.selectByUsername(request.getUsername());
        if (exists != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        entityValidationSupport.validate(user);

        userMapper.insert(user);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        entityValidationSupport.validate(user);
        userMapper.updatePassword(userId, user.getPassword());
    }

    @Override
    public void updateStatus(UserStatusUpdateRequest request) {
        SysUser user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        user.setStatus(request.getStatus());
        entityValidationSupport.validate(user);
        userMapper.updateStatus(request.getId(), request.getStatus());
    }

    @Override
    public void resetPassword(UserResetPasswordRequest request) {
        SysUser user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(defaultPassword));
        entityValidationSupport.validate(user);
        userMapper.updatePassword(request.getId(), user.getPassword());
    }
}


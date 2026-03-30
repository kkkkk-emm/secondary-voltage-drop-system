package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.PasswordChangeRequest;
import com.straykun.svd.svdsys.controller.dto.UserCreateRequest;
import com.straykun.svd.svdsys.controller.dto.UserPageQuery;
import com.straykun.svd.svdsys.controller.dto.UserResetPasswordRequest;
import com.straykun.svd.svdsys.controller.dto.UserStatusUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.UserVO;

/**
 * User service abstraction.
 */
public interface UserService {

    PageResult<UserVO> page(UserPageQuery query);

    void create(UserCreateRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);

    void updateStatus(UserStatusUpdateRequest request);

    void resetPassword(UserResetPasswordRequest request);
}


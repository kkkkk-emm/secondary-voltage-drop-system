package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser selectById(@Param("id") Long id);

    /**
     * 分页查询用户列表
     *
     * @param offset   偏移量
     * @param size     每页大小
     * @param username 用户名
     * @param role     角色
     * @param status   状态
     * @return 用户列表
     */
    java.util.List<SysUser> selectPage(@Param("offset") long offset,
            @Param("size") long size,
            @Param("username") String username,
            @Param("role") Integer role,
            @Param("status") Integer status);

    /**
     * 统计用户数量
     *
     * @param username 用户名
     * @param role     角色
     * @param status   状态
     * @return 用户数量
     */
    long count(@Param("username") String username,
            @Param("role") Integer role,
            @Param("status") Integer status);

    /**
     * 新增用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新用户密码
     *
     * @param id       用户ID
     * @param password 密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}

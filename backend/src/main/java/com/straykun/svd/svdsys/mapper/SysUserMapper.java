package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户数据访问接口，定义持久化操作。
 */
@Mapper
public interface SysUserMapper {

    /**
     * 查询 selectByUsername 相关信息。
     *
     * @param username 参数 username。
     * @return 返回处理结果。
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 查询 selectById 相关信息。
     *
     * @param id 参数 id。
     * @return 返回处理结果。
     */
    SysUser selectById(@Param("id") Long id);

    /**
     * 查询 selectPage 相关信息。
     *
     * @param offset 参数 offset。
     * @param size 参数 size。
     * @param username 参数 username。
     * @param role 参数 role。
     * @param status 参数 status。
     * @return 返回结果列表。
     */
    java.util.List<SysUser> selectPage(@Param("offset") long offset,
            @Param("size") long size,
            @Param("username") String username,
            @Param("role") Integer role,
            @Param("status") Integer status);

    /**
     * 查询 count 相关信息。
     *
     * @param username 参数 username。
     * @param role 参数 role。
     * @param status 参数 status。
     * @return 返回统计结果。
     */
    long count(@Param("username") String username,
            @Param("role") Integer role,
            @Param("status") Integer status);

    /**
     * 执行 insert 新增处理。
     *
     * @param user 参数 user。
     * @return 返回处理结果数量。
     */
    int insert(SysUser user);

    /**
     * 执行 updateStatus 更新处理。
     *
     * @param id 参数 id。
     * @param status 参数 status。
     * @return 返回处理结果数量。
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 执行 updatePassword 更新处理。
     *
     * @param id 参数 id。
     * @param password 参数 password。
     * @return 返回处理结果数量。
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}

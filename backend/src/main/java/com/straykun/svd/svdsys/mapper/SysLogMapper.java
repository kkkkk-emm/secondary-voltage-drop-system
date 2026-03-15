package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysLogMapper {

    /**
     * 新增日志
     *
     * @param log 日志信息
     * @return 影响行数
     */
    int insert(SysLog log);

    /**
     * 分页查询日志列表
     *
     * @param offset    偏移量
     * @param size      每页大小
     * @param username  用户名
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 日志列表
     */
    List<SysLog> selectPage(@Param("offset") long offset,
                           @Param("size") long size,
                           @Param("username") String username,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);

    /**
     * 统计日志数量
     *
     * @param username  用户名
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 日志数量
     */
    long count(@Param("username") String username,
               @Param("startDate") LocalDateTime startDate,
               @Param("endDate") LocalDateTime endDate);
}

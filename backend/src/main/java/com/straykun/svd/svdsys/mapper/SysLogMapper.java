package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志数据访问接口，定义持久化操作。
 */
@Mapper
public interface SysLogMapper {

    /**
     * 执行 insert 新增处理。
     *
     * @param log 参数 log。
     * @return 返回处理结果数量。
     */
    int insert(SysLog log);

    /**
     * 查询 selectPage 相关信息。
     *
     * @param offset 参数 offset。
     * @param size 参数 size。
     * @param username 参数 username。
     * @param startDate 参数 startDate。
     * @param endDate 参数 endDate。
     * @return 返回结果列表。
     */
    List<SysLog> selectPage(@Param("offset") long offset,
                           @Param("size") long size,
                           @Param("username") String username,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);

    /**
     * 查询 count 相关信息。
     *
     * @param username 参数 username。
     * @param startDate 参数 startDate。
     * @param endDate 参数 endDate。
     * @return 返回统计结果。
     */
    long count(@Param("username") String username,
               @Param("startDate") LocalDateTime startDate,
               @Param("endDate") LocalDateTime endDate);
}

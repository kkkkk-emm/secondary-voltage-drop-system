package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.BizTestResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizTestResultMapper {

    /**
     * 新增检测结果
     *
     * @param result 检测结果
     * @return 影响行数
     */
    int insert(BizTestResult result);

    /**
     * 批量新增检测结果
     *
     * @param list 检测结果列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<BizTestResult> list);

    /**
     * 根据任务ID查询检测结果列表
     *
     * @param taskId 任务ID
     * @return 检测结果列表
     */
    List<BizTestResult> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据ID查询检测结果
     *
     * @param id 结果ID
     * @return 检测结果
     */
    BizTestResult selectById(@Param("id") Long id);

    /**
     * 根据ID更新检测结果
     *
     * @param result 检测结果
     * @return 影响行数
     */
    int updateById(BizTestResult result);
}

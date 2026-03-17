package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.ResultCorrectRequest;
import com.straykun.svd.svdsys.controller.dto.TaskCreateDTO;
import com.straykun.svd.svdsys.controller.dto.TaskPageQuery;
import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;
import com.straykun.svd.svdsys.controller.vo.TaskListItemVO;

/**
 * 检定任务服务接口，定义业务能力。
 */
public interface TaskService {

    /**
     * 执行 submit 新增处理。
     *
     * @param dto 参数 dto。
     */
    void submit(TaskCreateDTO dto);

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    PageResult<TaskListItemVO> page(TaskPageQuery query);

    /**
     * 查询 detail 相关信息。
     *
     * @param id 参数 id。
     * @return 返回处理结果。
     */
    TaskDetailVO detail(Long id);

    /**
     * 执行 correctResult 更新处理。
     *
     * @param request 参数 request。
     */
    void correctResult(ResultCorrectRequest request);
}

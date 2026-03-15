package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.ResultCorrectRequest;
import com.straykun.svd.svdsys.controller.dto.TaskCreateDTO;
import com.straykun.svd.svdsys.controller.dto.TaskPageQuery;
import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;
import com.straykun.svd.svdsys.controller.vo.TaskListItemVO;

public interface TaskService {

    /**
     * 提交检测任务
     *
     * @param dto 任务信息
     */
    void submit(TaskCreateDTO dto);

    /**
     * 分页查询任务列表
     *
     * @param query 查询条件
     * @return 任务列表分页结果
     */
    PageResult<TaskListItemVO> page(TaskPageQuery query);

    /**
     * 查询任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDetailVO detail(Long id);

    /**
     * 数据修正（管理员专用）
     *
     * @param request 修正请求
     */
    void correctResult(ResultCorrectRequest request);
}

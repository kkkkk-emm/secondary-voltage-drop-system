package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.TaskCreateDTO;
import com.straykun.svd.svdsys.controller.dto.TaskPageQuery;
import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;
import com.straykun.svd.svdsys.controller.vo.TaskListItemVO;
import com.straykun.svd.svdsys.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 检定任务管理模块
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 提交检定任务（归档，仅检测员）
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    @SysLog("提交检定任务")
    public Result<Void> submit(@RequestBody @Valid TaskCreateDTO dto) {
        taskService.submit(dto);
        return Result.success();
    }

    /**
     * 分页查询检定记录（检测员查自己的，管理员可用于数据修正模块查看）
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<PageResult<TaskListItemVO>> page(TaskPageQuery query) {
        return Result.success(taskService.page(query));
    }

    /**
     * 获取检定任务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<TaskDetailVO> detail(@PathVariable Long id) {
        return Result.success(taskService.detail(id));
    }
}



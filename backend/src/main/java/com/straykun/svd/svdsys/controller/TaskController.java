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
 * 检定任务控制器。
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    /**
     * 构造函数，初始化 TaskController 所需依赖。
     *
     * @param taskService 参数 taskService。
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 执行 submit 新增处理。
     *
     * @param dto 参数 dto。
     * @return 返回统一响应结果。
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    @SysLog("提交检定任务")
    public Result<Void> submit(@RequestBody @Valid TaskCreateDTO dto) {
        taskService.submit(dto);
        return Result.success();
    }

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<PageResult<TaskListItemVO>> page(TaskPageQuery query) {
        return Result.success(taskService.page(query));
    }

    /**
     * 查询 detail 相关信息。
     *
     * @param id 参数 id。
     * @return 返回统一响应结果。
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<TaskDetailVO> detail(@PathVariable Long id) {
        return Result.success(taskService.detail(id));
    }
}

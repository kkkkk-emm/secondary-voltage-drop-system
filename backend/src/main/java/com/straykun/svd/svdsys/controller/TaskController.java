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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Task controller.
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    @SysLog("Submit task")
    public Result<Void> submit(@RequestBody @Valid TaskCreateDTO dto) {
        taskService.submit(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<PageResult<TaskListItemVO>> page(TaskPageQuery query) {
        return Result.success(taskService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Result<TaskDetailVO> detail(@PathVariable Long id) {
        return Result.success(taskService.detail(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @SysLog("Update task")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid TaskCreateDTO dto) {
        taskService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @SysLog("Delete task")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }
}

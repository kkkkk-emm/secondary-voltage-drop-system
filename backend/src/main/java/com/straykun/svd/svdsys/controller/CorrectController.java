package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.ResultCorrectRequest;
import com.straykun.svd.svdsys.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据修正模块（管理员专用）
 */
@RestController
@RequestMapping("/correct")
public class CorrectController {

    private final TaskService taskService;

    public CorrectController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 修正检测结果数据
     */
    @PostMapping("/result")
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("数据修正")
    public Result<Void> correctResult(@RequestBody @Valid ResultCorrectRequest request) {
        taskService.correctResult(request);
        return Result.success();
    }
}

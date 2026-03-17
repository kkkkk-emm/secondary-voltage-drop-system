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
 * 数据修正控制器。
 */
@RestController
@RequestMapping("/correct")
public class CorrectController {

    private final TaskService taskService;

    /**
     * 构造函数，初始化 CorrectController 所需依赖。
     *
     * @param taskService 参数 taskService。
     */
    public CorrectController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 执行 correctResult 更新处理。
     *
     * @param request 参数 request。
     * @return 返回统一响应结果。
     */
    @PostMapping("/result")
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("数据修正")
    public Result<Void> correctResult(@RequestBody @Valid ResultCorrectRequest request) {
        taskService.correctResult(request);
        return Result.success();
    }
}

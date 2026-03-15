package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.LogPageQuery;
import com.straykun.svd.svdsys.controller.vo.LogVO;
import com.straykun.svd.svdsys.service.LogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统日志审计接口（仅管理员）
 */
@RestController
@RequestMapping("/log")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * 分页查询系统日志
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<LogVO>> page(LogPageQuery query) {
        return Result.success(logService.page(query));
    }
}


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
 * 日志控制器。
 */
@RestController
@RequestMapping("/log")
public class LogController {

    private final LogService logService;

    /**
     * 构造函数，初始化 LogController 所需依赖。
     *
     * @param logService 参数 logService。
     */
    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<LogVO>> page(LogPageQuery query) {
        return Result.success(logService.page(query));
    }
}

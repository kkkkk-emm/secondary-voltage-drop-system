package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.vo.DashboardStatsVO;
import com.straykun.svd.svdsys.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 看板控制器。
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 构造函数，初始化 DashboardController 所需依赖。
     *
     * @param dashboardService 参数 dashboardService。
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 查询 getStats 相关信息。
     *
     * @return 返回统一响应结果。
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getStats());
    }
}

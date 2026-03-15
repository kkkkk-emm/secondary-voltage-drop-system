package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.DashboardStatsVO;

/**
 * 数据可视化统计服务
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     */
    DashboardStatsVO getStats();
}

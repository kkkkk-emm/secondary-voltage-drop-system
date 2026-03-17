package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.DashboardStatsVO;

/**
 * 看板服务接口，定义业务能力。
 */
public interface DashboardService {

    /**
     * 查询 getStats 相关信息。
     *
     * @return 返回处理结果。
     */
    DashboardStatsVO getStats();
}

package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.LogPageQuery;
import com.straykun.svd.svdsys.controller.vo.LogVO;

import java.util.List;

/**
 * 日志服务接口，定义业务能力。
 */
public interface LogService {

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    PageResult<LogVO> page(LogPageQuery query);
}

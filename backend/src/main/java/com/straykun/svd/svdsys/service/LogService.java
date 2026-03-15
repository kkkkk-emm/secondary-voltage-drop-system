package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.LogPageQuery;
import com.straykun.svd.svdsys.controller.vo.LogVO;

import java.util.List;

/**
 * 系统日志服务
 */
public interface LogService {

    /**
     * 分页查询日志
     */
    PageResult<LogVO> page(LogPageQuery query);
}


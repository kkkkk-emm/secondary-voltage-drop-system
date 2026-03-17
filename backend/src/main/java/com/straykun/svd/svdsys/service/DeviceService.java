package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.DevicePageQuery;
import com.straykun.svd.svdsys.controller.dto.DeviceSaveRequest;
import com.straykun.svd.svdsys.controller.dto.DeviceUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.DeviceVO;

import java.util.List;

/**
 * 设备服务接口，定义业务能力。
 */
public interface DeviceService {

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    PageResult<DeviceVO> page(DevicePageQuery query);

    /**
     * 执行 add 业务逻辑。
     *
     * @param request 参数 request。
     */
    void add(DeviceSaveRequest request);

    /**
     * 执行 update 更新处理。
     *
     * @param request 参数 request。
     */
    void update(DeviceUpdateRequest request);

    /**
     * 执行 delete 删除处理。
     *
     * @param id 参数 id。
     */
    void delete(Long id);

    /**
     * 查询 listAll 相关信息。
     *
     * @return 返回结果列表。
     */
    List<DeviceVO> listAll();
}

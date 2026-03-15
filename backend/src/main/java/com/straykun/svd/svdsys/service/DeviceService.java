package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.DevicePageQuery;
import com.straykun.svd.svdsys.controller.dto.DeviceSaveRequest;
import com.straykun.svd.svdsys.controller.dto.DeviceUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.DeviceVO;

import java.util.List;

public interface DeviceService {

    /**
     * 分页查询设备列表
     *
     * @param query 查询条件
     * @return 设备列表分页结果
     */
    PageResult<DeviceVO> page(DevicePageQuery query);

    /**
     * 新增设备
     *
     * @param request 设备信息
     */
    void add(DeviceSaveRequest request);

    /**
     * 更新设备
     *
     * @param request 设备信息
     */
    void update(DeviceUpdateRequest request);

    /**
     * 删除设备
     *
     * @param id 设备ID
     */
    void delete(Long id);

    /**
     * 获取所有设备列表（用于下拉选择）
     *
     * @return 所有设备列表
     */
    List<DeviceVO> listAll();
}

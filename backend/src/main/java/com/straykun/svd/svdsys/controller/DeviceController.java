package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.DevicePageQuery;
import com.straykun.svd.svdsys.controller.dto.DeviceSaveRequest;
import com.straykun.svd.svdsys.controller.dto.DeviceUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.DeviceVO;
import com.straykun.svd.svdsys.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 被检设备管理
 *
 * 根据用例图：该模块主要由"检测员"使用。
 * 权限约定：
 * - 所有设备管理接口仅检测员(USER)可访问
 */
@RestController
@RequestMapping("/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 分页查询设备列表（仅检测员）
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('USER')")
    public Result<PageResult<DeviceVO>> page(DevicePageQuery query) {
        return Result.success(deviceService.page(query));
    }

    /**
     * 获取所有设备列表（用于下拉选择，仅检测员）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<java.util.List<DeviceVO>> list() {
        return Result.success(deviceService.listAll());
    }

    /**
     * 新增设备档案（仅检测员）
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @SysLog("新增设备")
    public Result<Void> add(@RequestBody @Valid DeviceSaveRequest request) {
        deviceService.add(request);
        return Result.success();
    }

    /**
     * 修改设备档案（仅检测员）
     */
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @SysLog("修改设备")
    public Result<Void> update(@RequestBody @Valid DeviceUpdateRequest request) {
        deviceService.update(request);
        return Result.success();
    }

    /**
     * 删除设备（仅检测员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @SysLog("删除设备")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }
}



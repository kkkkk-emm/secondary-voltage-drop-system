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
 * 设备控制器。
 */
@RestController
@RequestMapping("/device")
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 构造函数，初始化 DeviceController 所需依赖。
     *
     * @param deviceService 参数 deviceService。
     */
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('USER')")
    public Result<PageResult<DeviceVO>> page(DevicePageQuery query) {
        return Result.success(deviceService.page(query));
    }

    /**
     * 查询 list 相关信息。
     *
     * @return 返回统一响应结果。
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public Result<java.util.List<DeviceVO>> list() {
        return Result.success(deviceService.listAll());
    }

    /**
     * 执行 add 业务逻辑。
     *
     * @param request 参数 request。
     * @return 返回统一响应结果。
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @SysLog("新增设备")
    public Result<Void> add(@RequestBody @Valid DeviceSaveRequest request) {
        deviceService.add(request);
        return Result.success();
    }

    /**
     * 执行 update 更新处理。
     *
     * @param request 参数 request。
     * @return 返回统一响应结果。
     */
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @SysLog("修改设备")
    public Result<Void> update(@RequestBody @Valid DeviceUpdateRequest request) {
        deviceService.update(request);
        return Result.success();
    }

    /**
     * 执行 delete 删除处理。
     *
     * @param id 参数 id。
     * @return 返回统一响应结果。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @SysLog("删除设备")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }
}

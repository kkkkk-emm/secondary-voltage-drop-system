package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.dto.StandardUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.StandardGroupVO;
import com.straykun.svd.svdsys.controller.vo.StandardVO;
import com.straykun.svd.svdsys.service.StandardService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检定标准配置模块
 */
@RestController
@RequestMapping("/standard")
public class StandardController {

    private final StandardService standardService;

    public StandardController(StandardService standardService) {
        this.standardService = standardService;
    }

    /**
     * 获取标准配置列表（包含每一项数据）
     */
    @GetMapping("/list")
    public Result<List<StandardVO>> list() {
        return Result.success(standardService.listAll());
    }

    /**
     * 获取分组后的标准列表（用于下拉框选择，去重）
     */
    @GetMapping("/groups")
    public Result<List<StandardGroupVO>> listGroups() {
        return Result.success(standardService.listGroups());
    }

    /**
     * 匹配特定标准（用于实时自动校验）- 旧接口保留兼容
     */
    @GetMapping("/match")
    public Result<Map<String, Object>> match(@RequestParam String projectType,
                                             @RequestParam String gearLevel,
                                             @RequestParam String loadPercent) {
        StandardService.StandardLimit limit = standardService.match(projectType, gearLevel, loadPercent);
        if (limit == null) {
            return Result.success(null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("limitMin", limit.getLimitMin());
        data.put("limitMax", limit.getLimitMax());
        return Result.success(data);
    }

    /**
     * 获取指定组合下的所有阈值项（用于新的多项校验）
     */
    @GetMapping("/matchAll")
    public Result<StandardGroupVO> matchAll(@RequestParam String projectType,
                                            @RequestParam String gearLevel,
                                            @RequestParam String loadPercent) {
        StandardGroupVO group = standardService.matchAllThresholds(projectType, gearLevel, loadPercent);
        return Result.success(group);
    }

    /**
     * 修改阈值（仅管理员）
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("修改标准阈值")
    public Result<Void> update(@RequestBody @Valid StandardUpdateRequest request) {
        standardService.update(request);
        return Result.success();
    }
}



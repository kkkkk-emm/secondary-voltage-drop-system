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
 * 标准配置控制器。
 */
@RestController
@RequestMapping("/standard")
public class StandardController {

    private final StandardService standardService;

    /**
     * 构造函数，初始化 StandardController 所需依赖。
     *
     * @param standardService 参数 standardService。
     */
    public StandardController(StandardService standardService) {
        this.standardService = standardService;
    }

    /**
     * 查询 list 相关信息。
     *
     * @return 返回统一响应结果。
     */
    @GetMapping("/list")
    public Result<List<StandardVO>> list() {
        return Result.success(standardService.listAll());
    }

    /**
     * 查询 listGroups 相关信息。
     *
     * @return 返回统一响应结果。
     */
    @GetMapping("/groups")
    public Result<List<StandardGroupVO>> listGroups() {
        return Result.success(standardService.listGroups());
    }

    /**
     * 按项目类型、档位与负载匹配单项标准阈值范围。
     *
     * @param projectType 项目类型。
     * @param gearLevel 档位。
     * @param loadPercent 负载百分比。
     * @return 返回统一响应结果。
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
     * 执行 matchAll 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回统一响应结果。
     */
    @GetMapping("/matchAll")
    public Result<StandardGroupVO> matchAll(@RequestParam String projectType,
                                            @RequestParam String gearLevel,
                                            @RequestParam String loadPercent) {
        StandardGroupVO group = standardService.matchAllThresholds(projectType, gearLevel, loadPercent);
        return Result.success(group);
    }

    /**
     * 执行 update 更新处理。
     *
     * @param request 参数 request。
     * @return 返回统一响应结果。
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SysLog("修改标准阈值")
    public Result<Void> update(@RequestBody @Valid StandardUpdateRequest request) {
        standardService.update(request);
        return Result.success();
    }
}

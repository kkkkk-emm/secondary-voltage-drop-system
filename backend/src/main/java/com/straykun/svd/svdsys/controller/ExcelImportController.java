package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.vo.ExcelImportResultVO;
import com.straykun.svd.svdsys.controller.vo.ExcelPreviewVO;
import com.straykun.svd.svdsys.service.ExcelImportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 导入控制器。
 */
@RestController
@RequestMapping("/import")
public class ExcelImportController {

    private final ExcelImportService excelImportService;

    /**
     * 构造函数，初始化 ExcelImportController 所需依赖。
     *
     * @param excelImportService 参数 excelImportService。
     */
    public ExcelImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    /**
     * 执行 previewTasks 业务逻辑。
     *
     * @param file 参数 file。
     * @return 返回统一响应结果。
     */
    @PostMapping("/tasks/preview")
    @PreAuthorize("hasRole('USER')")
    public Result<ExcelPreviewVO> previewTasks(@RequestParam("file") MultipartFile file) {
        ExcelPreviewVO result = excelImportService.previewTasks(file);
        return Result.success(result);
    }

    /**
     * 执行 previewDevices 业务逻辑。
     *
     * @param file 参数 file。
     * @return 返回统一响应结果。
     */
    @PostMapping("/devices/preview")
    @PreAuthorize("hasRole('USER')")
    public Result<ExcelPreviewVO> previewDevices(@RequestParam("file") MultipartFile file) {
        ExcelPreviewVO result = excelImportService.previewDevices(file);
        return Result.success(result);
    }

    /**
     * 执行 importTasks 新增处理。
     *
     * @param file 参数 file。
     * @return 返回统一响应结果。
     */
    @PostMapping("/tasks")
    @PreAuthorize("hasRole('USER')")
    @SysLog("从Excel导入检定任务")
    public Result<ExcelImportResultVO> importTasks(@RequestParam("file") MultipartFile file) {
        ExcelImportResultVO result = excelImportService.importTasks(file);
        return Result.success(result);
    }

    /**
     * 执行 importDevices 新增处理。
     *
     * @param file 参数 file。
     * @return 返回统一响应结果。
     */
    @PostMapping("/devices")
    @PreAuthorize("hasRole('USER')")
    @SysLog("从Excel导入设备数据")
    public Result<ExcelImportResultVO> importDevices(@RequestParam("file") MultipartFile file) {
        ExcelImportResultVO result = excelImportService.importDevices(file);
        return Result.success(result);
    }
}

package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.ExcelImportResultVO;
import com.straykun.svd.svdsys.controller.vo.ExcelPreviewVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel导入服务接口，定义业务能力。
 */
public interface ExcelImportService {

    /**
     * 执行 previewTasks 业务逻辑。
     *
     * @param file 参数 file。
     * @return 返回处理结果。
     */
    ExcelPreviewVO previewTasks(MultipartFile file);

    /**
     * 执行 previewDevices 业务逻辑。
     *
     * @param file 参数 file。
     * @return 返回处理结果。
     */
    ExcelPreviewVO previewDevices(MultipartFile file);

    /**
     * 执行 importTasks 新增处理。
     *
     * @param file 参数 file。
     * @return 返回处理结果。
     */
    ExcelImportResultVO importTasks(MultipartFile file);

    /**
     * 执行 importDevices 新增处理。
     *
     * @param file 参数 file。
     * @return 返回处理结果。
     */
    ExcelImportResultVO importDevices(MultipartFile file);
}

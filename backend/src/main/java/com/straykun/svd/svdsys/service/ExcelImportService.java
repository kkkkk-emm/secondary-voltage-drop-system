package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.ExcelImportResultVO;
import com.straykun.svd.svdsys.controller.vo.ExcelPreviewVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 导入服务接口
 */
public interface ExcelImportService {

    /**
     * 预览检定任务 Excel 数据（不入库）
     *
     * @param file Excel 文件
     * @return 预览结果
     */
    ExcelPreviewVO previewTasks(MultipartFile file);

    /**
     * 预览设备 Excel 数据（不入库）
     *
     * @param file Excel 文件
     * @return 预览结果
     */
    ExcelPreviewVO previewDevices(MultipartFile file);

    /**
     * 从 Excel 导入检定任务数据
     * Excel 格式要求：
     * | 设备编号 | 计量点编号 | 送检日期 | 测试日期 | 温度 | 湿度 | tanφ | r% |
     * | 相别 | 项目类型 | 档位 | 负载% | f(%) | δ(分) | dU(%) | Upt | Uyb |
     *
     * @param file Excel 文件（.xlsx）
     * @return 导入结果
     */
    ExcelImportResultVO importTasks(MultipartFile file);

    /**
     * 从 Excel 导入设备数据
     * Excel 格式要求：
     * | 产品编号 | 产品名称 | 型号 | 制造商 | 产地 | 生产日期 |
     *
     * @param file Excel 文件（.xlsx）
     * @return 导入结果
     */
    ExcelImportResultVO importDevices(MultipartFile file);
}

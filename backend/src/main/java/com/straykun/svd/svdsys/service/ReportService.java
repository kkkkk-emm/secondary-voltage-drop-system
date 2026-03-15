package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;

import java.io.OutputStream;

/**
 * 报告生成服务接口
 */
public interface ReportService {

    /**
     * 生成检定报告 PDF
     *
     * @param detail 任务详情
     * @param outputStream 输出流
     */
    void generatePdf(TaskDetailVO detail, OutputStream outputStream);
}

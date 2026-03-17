package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;

import java.io.OutputStream;

/**
 * 报告服务接口，定义业务能力。
 */
public interface ReportService {

    /**
     * 执行 generatePdf 业务逻辑。
     *
     * @param detail 参数 detail。
     * @param outputStream 参数 outputStream。
     */
    void generatePdf(TaskDetailVO detail, OutputStream outputStream);
}

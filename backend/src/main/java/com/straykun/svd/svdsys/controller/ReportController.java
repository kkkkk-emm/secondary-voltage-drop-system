package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;
import com.straykun.svd.svdsys.service.ReportService;
import com.straykun.svd.svdsys.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 检定报告预览/下载
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";

    private final TaskService taskService;
    private final ReportService reportService;

    public ReportController(TaskService taskService, ReportService reportService) {
        this.taskService = taskService;
        this.reportService = reportService;
    }

    /**
     * 预览 PDF 检定证书（在浏览器中打开）
     */
    @GetMapping("/preview/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public void preview(@PathVariable Long taskId, HttpServletResponse response) throws IOException {
        TaskDetailVO detail = taskService.detail(taskId);
        if (detail == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
            response.getWriter().write("{\"code\":404,\"message\":\"任务不存在\"}");
            return;
        }

        try {
            // 先生成到内存，避免流被占用后无法修改响应
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            reportService.generatePdf(detail, baos);
            
            byte[] pdfBytes = baos.toByteArray();
            response.setContentType("application/pdf");
            response.setContentLength(pdfBytes.length);
            response.setHeader("Content-Disposition", "inline; filename=\"report-" + taskId + ".pdf\"");
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("PDF生成失败, taskId={}", taskId, e);
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
            response.getWriter().write("{\"code\":500,\"message\":\"PDF生成失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 下载 PDF 检定证书
     */
    @GetMapping("/download/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public void download(@PathVariable Long taskId, HttpServletResponse response) throws IOException {
        TaskDetailVO detail = taskService.detail(taskId);
        if (detail == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
            response.getWriter().write("{\"code\":404,\"message\":\"任务不存在\"}");
            return;
        }

        // 构建文件名
        String deviceNo = detail.getDeviceInfo() != null && detail.getDeviceInfo().getProductNo() != null
                ? detail.getDeviceInfo().getProductNo()
                : "unknown";
        String fileName = "检定证书_" + deviceNo + "_" + taskId + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        try {
            // 先生成到内存
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            reportService.generatePdf(detail, baos);
            
            byte[] pdfBytes = baos.toByteArray();
            response.setContentType("application/pdf");
            response.setContentLength(pdfBytes.length);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("PDF下载失败, taskId={}", taskId, e);
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
            response.getWriter().write("{\"code\":500,\"message\":\"PDF生成失败: " + e.getMessage() + "\"}");
        }
    }
}



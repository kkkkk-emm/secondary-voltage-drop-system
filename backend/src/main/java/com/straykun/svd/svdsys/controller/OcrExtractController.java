package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.RateLimitExceededException;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.vo.OcrExtractResponseVO;
import com.straykun.svd.svdsys.service.OcrExtractService;
import com.straykun.svd.svdsys.service.OcrRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR 提取控制器。
 */
@RestController
@RequestMapping("/ocr")
public class OcrExtractController {

    private static final Logger log = LoggerFactory.getLogger(OcrExtractController.class);

    private final OcrExtractService ocrExtractService;
    private final OcrRateLimitService ocrRateLimitService;

    /**
     * 构造函数，初始化 OcrExtractController 所需依赖。
     *
     * @param ocrExtractService 参数 ocrExtractService。
     * @param ocrRateLimitService 参数 ocrRateLimitService。
     */
    public OcrExtractController(OcrExtractService ocrExtractService,
                                OcrRateLimitService ocrRateLimitService) {
        this.ocrExtractService = ocrExtractService;
        this.ocrRateLimitService = ocrRateLimitService;
    }

    /**
     * 执行 extract 数据处理。
     *
     * @param file 参数 file。
     * @param request 参数 request。
     * @return 返回统一响应结果。
     */
    @PostMapping("/extract")
    @PreAuthorize("hasRole('USER')")
    @SysLog("图片OCR提取")
    public Result<OcrExtractResponseVO> extract(@RequestParam("file") MultipartFile file,
                                                HttpServletRequest request) {
        try {
            ocrRateLimitService.check(request);
            return Result.success(ocrExtractService.extract(file));
        } catch (RateLimitExceededException e) {
            log.warn("OCR接口触发限流: {}", e.getMessage());
            return Result.fail(429, e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("OCR提取业务异常: {}", e.getMessage());
            return Result.fail(500, e.getMessage());
        } catch (Exception e) {
            log.error("OCR提取系统异常: {}", e.getMessage(), e);
            return Result.fail(500, "图片识别失败，请稍后重试");
        }
    }
}

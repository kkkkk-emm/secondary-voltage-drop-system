package com.straykun.svd.svdsys.controller;

import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.common.Result;
import com.straykun.svd.svdsys.controller.vo.OcrExtractResponseVO;
import com.straykun.svd.svdsys.service.OcrExtractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片 OCR 抽取控制器
 */
@RestController
@RequestMapping("/ocr")
public class OcrExtractController {

    private static final Logger log = LoggerFactory.getLogger(OcrExtractController.class);

    private final OcrExtractService ocrExtractService;

    public OcrExtractController(OcrExtractService ocrExtractService) {
        this.ocrExtractService = ocrExtractService;
    }

    /**
     * 图片上传 -> OCR -> 大模型键值对抽取
     */
    @PostMapping("/extract")
    @PreAuthorize("hasRole('USER')")
    @SysLog("图片OCR提取")
    public Result<OcrExtractResponseVO> extract(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success(ocrExtractService.extract(file));
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("OCR提取流程失败: {}", e.getMessage());
            return Result.fail(500, e.getMessage());
        } catch (Exception e) {
            log.error("OCR提取异常: {}", e.getMessage(), e);
            return Result.fail(500, "图片识别失败，请稍后重试");
        }
    }
}


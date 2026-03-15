package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.OcrExtractResponseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片 OCR + 大模型抽取服务
 */
public interface OcrExtractService {

    /**
     * 从图片中识别文本并抽取固定字段
     *
     * @param file 图片文件
     * @return 提取结果
     */
    OcrExtractResponseVO extract(MultipartFile file);
}


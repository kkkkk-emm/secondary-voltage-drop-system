package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.vo.OcrExtractResponseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR提取服务接口，定义业务能力。
 */
public interface OcrExtractService {

    /**
     * 执行 extract 数据处理。
     *
     * @param file 参数 file。
     * @return 返回处理结果。
     */
    OcrExtractResponseVO extract(MultipartFile file);
}

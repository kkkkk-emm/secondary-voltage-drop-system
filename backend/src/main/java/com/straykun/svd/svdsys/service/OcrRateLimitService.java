package com.straykun.svd.svdsys.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * OCR速率限制服务接口，定义业务能力。
 */
public interface OcrRateLimitService {

    /**
     * 执行 check 校验逻辑。
     *
     * @param request 参数 request。
     */
    void check(HttpServletRequest request);
}

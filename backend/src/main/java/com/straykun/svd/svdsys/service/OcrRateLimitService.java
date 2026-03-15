package com.straykun.svd.svdsys.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * OCR 接口限流服务
 */
public interface OcrRateLimitService {

    /**
     * 校验当前请求是否超过 OCR 频率限制
     *
     * @param request 当前 HTTP 请求
     */
    void check(HttpServletRequest request);
}


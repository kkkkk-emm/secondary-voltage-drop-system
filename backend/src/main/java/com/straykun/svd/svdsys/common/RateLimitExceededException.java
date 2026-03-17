package com.straykun.svd.svdsys.common;

/**
 * 限流超限异常。
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    /**
     * 构造函数，初始化 RateLimitExceededException 所需依赖。
     *
     * @param message 参数 message。
     * @param retryAfterSeconds 参数 retryAfterSeconds。
     */
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 查询 getRetryAfterSeconds 相关信息。
     *
     * @return 返回处理结果数量。
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

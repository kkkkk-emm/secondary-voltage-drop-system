package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.common.RateLimitExceededException;
import com.straykun.svd.svdsys.security.SecurityUtils;
import com.straykun.svd.svdsys.service.OcrRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * OCR 限流服务实现类。
 */
@Service
public class OcrRateLimitServiceImpl implements OcrRateLimitService {

    private static final String UNKNOWN = "unknown";
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<Long> counterScript;

    @Value("${ocr.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${ocr.rate-limit.max-requests:15}")
    private int maxRequests;

    @Value("${ocr.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${ocr.rate-limit.key-prefix:svd:rate:ocr}")
    private String keyPrefix;

    /**
     * 构造函数，初始化 OcrRateLimitServiceImpl 所需依赖。
     *
     * @param stringRedisTemplate 参数 stringRedisTemplate。
     */
    public OcrRateLimitServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.counterScript = buildCounterScript();
    }

    /**
     * 执行 check 校验逻辑。
     *
     * @param request 参数 request。
     */
    @Override
    public void check(HttpServletRequest request) {
        if (!rateLimitEnabled) {
            return;
        }

        String key = buildRateLimitKey(request);
        int safeWindowSeconds = Math.max(windowSeconds, 1);
        int safeMaxRequests = Math.max(maxRequests, 1);

        try {
            Long current = stringRedisTemplate.execute(
                    counterScript,
                    Collections.singletonList(key),
                    String.valueOf(safeWindowSeconds) // ARGV[1]：窗口期秒数
            );

            if (current == null) {
                throw new IllegalStateException("限流服务暂不可用，已拒绝OCR请求，请稍后重试");
            }

            if (current > safeMaxRequests) {
                Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
                long retryAfterSeconds = ttl != null && ttl > 0 ? ttl : safeWindowSeconds;
                throw new RateLimitExceededException(
                        "请求过于频繁，请" + retryAfterSeconds + "秒后重试",
                        retryAfterSeconds
                );
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("限流服务暂不可用，已拒绝OCR请求，请稍后重试", e);
        }
    }

    private RedisScript<Long> buildCounterScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "local current = redis.call('INCR', KEYS[1])\n" +
                        "if tonumber(current) == 1 then\n" +
                        "  redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
                        "end\n" +
                        "return current"
        );
        return script;
    }

    private String buildRateLimitKey(HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        if (StringUtils.hasText(userId)) {
            return keyPrefix + ":user:" + userId;
        }
        return keyPrefix + ":ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        for (String header : IP_HEADERS) {
            String candidate = request.getHeader(header);
            if (isValidIp(candidate)) {
                if (candidate.contains(",")) {
                    return candidate.split(",")[0].trim();
                }
                return candidate.trim();
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : UNKNOWN;
    }

    private boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip.trim());
    }
}

package com.straykun.svd.svdsys.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.straykun.svd.svdsys.annotation.SysLog;
import com.straykun.svd.svdsys.mapper.SysLogMapper;
import com.straykun.svd.svdsys.mapper.SysUserMapper;
import com.straykun.svd.svdsys.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统日志切面：自动记录带@SysLog注解的方法调用
 */
@Aspect
@Component
public class SysLogAspect {
    // 定义需要检查的 Header 数组，按优先级排序
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };
    private static final String UNKNOWN = "unknown";

    private static final Logger logger = LoggerFactory.getLogger(SysLogAspect.class);

    private final SysLogMapper sysLogMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    public SysLogAspect(SysLogMapper sysLogMapper, SysUserMapper sysUserMapper, ObjectMapper objectMapper) {
        this.sysLogMapper = sysLogMapper;
        this.sysUserMapper = sysUserMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.straykun.svd.svdsys.annotation.SysLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long time = System.currentTimeMillis() - startTime;
            saveLog(joinPoint, time, exception);
        }

        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time, Exception exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLog sysLogAnnotation = method.getAnnotation(SysLog.class);

        // 1. 卫语句：如果没有注解，直接结束
        if (sysLogAnnotation == null) {
            return;
        }

        try {
            com.straykun.svd.svdsys.domain.SysLog log = new com.straykun.svd.svdsys.domain.SysLog();

            // 2. 基础信息
            log.setOperation(sysLogAnnotation.value());
            log.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
            log.setTime(time);
            log.setCreateTime(LocalDateTime.now());

            // 3. 调用辅助方法设置复杂字段 (消除了嵌套 Try-Catch)
            log.setParams(getMethodParams(joinPoint.getArgs()));
            log.setIp(getRequestIp());
            log.setUsername(getCurrentUsername());

            // 4. 入库
            sysLogMapper.insert(log);

        } catch (Exception e) {
            // 记录日志失败不应影响业务，但要打印错误堆栈
            logger.warn("记录操作日志失败", e);
        }
    }

    // 辅助方法 1：安全地获取并序列化参数
    private String getMethodParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            List<Object> filteredArgs = new ArrayList<>();
            for (Object arg : args) {
                // 使用 Java 14+ 模式匹配
                if (arg instanceof MultipartFile file) {
                    filteredArgs.add("File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                } else if (arg != null && !isFilterObject(arg)) {
                    filteredArgs.add(arg);
                }
            }
            return objectMapper.writeValueAsString(filteredArgs);
        } catch (Exception e) {
            return "参数序列化失败";
        }
    }

    // 辅助判断：是否是需要过滤的对象（Spring 的一些内置对象不能序列化）
    private boolean isFilterObject(Object arg) {
        String className = arg.getClass().getName();
        return className.startsWith("org.springframework")
                || className.startsWith("jakarta.servlet") // Spring Boot 3+ 用 jakarta
                || className.startsWith("javax.servlet");  // 老版本 Spring 用 javax
    }

    // 辅助方法 2：获取当前操作用户名
    private String getCurrentUsername() {
        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            return null;
        }
        try {
            Long userId = Long.valueOf(userIdStr);
            var user = sysUserMapper.selectById(userId);
            return (user != null) ? user.getUsername() : null;
        } catch (Exception e) {
            // ID 格式错误或查无此人，忽略即可
            return null;
        }
    }

    // 辅助方法 3：获取请求 IP (复用之前的逻辑)
    private String getRequestIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return getIpAddress(attributes.getRequest());
    }

    private String getIpAddress(HttpServletRequest request) {
        // 1. 循环检查所有 Header
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            // 如果找到有效的 IP，直接返回（快速返回机制）
            if (isValidIp(ip)) {
                // 💡 进阶处理：X-Forwarded-For 可能会包含多个 IP (如 "client, proxy1, proxy2")
                // 通常第一个才是真实客户端 IP
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }

        // 2. 兜底：如果上面都没拿到，就用基础的 getRemoteAddr
        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}


package com.straykun.svd.svdsys.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 执行 handleMethodArgumentNotValid 业务逻辑。
     *
     * @param e 参数 e。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.fail(400, msg);
    }

    /**
     * 执行 handleBindException 业务逻辑。
     *
     * @param e 参数 e。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        return Result.fail(400, msg);
    }

    /**
     * 处理常见请求参数异常并返回 400 响应。
     *
     * @param e 参数异常对象。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler({MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class})
    public Result<Void> handleBadRequest(Exception e) {
        return Result.fail(400, "请求参数错误");
    }

    /**
     * 处理认证失败异常并返回 401 响应。
     *
     * @param e 认证异常对象。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthException(AuthenticationException e) {
        return Result.fail(401, "未认证或登录已过期");
    }

    /**
     * 处理无权限访问异常并返回 403 响应。
     *
     * @param e 权限异常对象。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        return Result.fail(403, "无访问权限");
    }

    /**
     * 处理请求方法不支持异常并返回 400 响应。
     *
     * @param e 请求方法异常对象。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(400, "不支持的请求方法");
    }

    /**
     * 兜底处理未捕获异常并返回 500 响应。
     *
     * @param e 系统异常对象。
     * @return 返回统一响应结果。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: [{}] {}", e.getClass().getName(), e.getMessage(), e);
        return Result.fail(500, "系统异常，请稍后重试");
    }
}

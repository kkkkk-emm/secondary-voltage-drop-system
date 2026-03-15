package com.straykun.svd.svdsys.common;

import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> {

    /**
     * 业务状态码：200-成功, 500-系统异常, 401-未认证, 403-无权限, 400-参数错误
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}



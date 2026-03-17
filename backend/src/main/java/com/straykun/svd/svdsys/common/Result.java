package com.straykun.svd.svdsys.common;

import lombok.Data;

/**
 * 统一响应结果对象。
 */
@Data
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    /**
     * 执行 success 业务逻辑。
     *
     * @param data 参数 data。
     * @return 返回统一响应结果。
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 执行 success 业务逻辑。
     *
     * @return 返回统一响应结果。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 执行 fail 业务逻辑。
     *
     * @param code 参数 code。
     * @param message 参数 message。
     * @return 返回统一响应结果。
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    /**
     * 执行 fail 业务逻辑。
     *
     * @param message 参数 message。
     * @return 返回统一响应结果。
     */
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}

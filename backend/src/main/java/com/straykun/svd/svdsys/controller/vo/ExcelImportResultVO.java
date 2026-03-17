package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入结果响应视图对象。
 */
@Data
public class ExcelImportResultVO {

    /**
     * totalRows 字段。
     */
    private int totalRows;

    /**
     * successRows 字段。
     */
    private int successRows;

    /**
     * failedRows 字段。
     */
    private int failedRows;

    private List<ErrorDetail> errors = new ArrayList<>();

    /**
     * Error详情响应视图对象。
     */
    @Data
    public static class ErrorDetail {
        /**
         * rowNum 字段。
         */
        private int rowNum;

        /**
         * 响应消息。
         */
        private String message;

        /**
         * 构造函数，初始化 ErrorDetail 所需依赖。
         *
         * @param rowNum 参数 rowNum。
         * @param message 参数 message。
         */
        public ErrorDetail(int rowNum, String message) {
            this.rowNum = rowNum;
            this.message = message;
        }
    }

    /**
     * 执行 addError 业务逻辑。
     *
     * @param rowNum 参数 rowNum。
     * @param message 参数 message。
     */
    public void addError(int rowNum, String message) {
        this.errors.add(new ErrorDetail(rowNum, message));
        this.failedRows++;
    }

    /**
     * 执行 incrementSuccess 业务逻辑。
     */
    public void incrementSuccess() {
        this.successRows++;
    }
}

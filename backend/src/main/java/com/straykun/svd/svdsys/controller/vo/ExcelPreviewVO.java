package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel预览响应视图对象。
 */
@Data
public class ExcelPreviewVO {

    /**
     * totalRows 字段。
     */
    private int totalRows;

    private List<String> headers = new ArrayList<>();

    private List<Map<String, Object>> rows = new ArrayList<>();

    private List<ValidationError> validationErrors = new ArrayList<>();

    /**
     * valid 字段。
     */
    private boolean valid;

    /**
     * ValidationError响应视图对象。
     */
    @Data
    public static class ValidationError {
        /**
         * rowNum 字段。
         */
        private int rowNum;

        /**
         * columnName 字段。
         */
        private String columnName;

        /**
         * 响应消息。
         */
        private String message;

        /**
         * 构造函数，初始化 ValidationError 所需依赖。
         *
         * @param rowNum 参数 rowNum。
         * @param columnName 参数 columnName。
         * @param message 参数 message。
         */
        public ValidationError(int rowNum, String columnName, String message) {
            this.rowNum = rowNum;
            this.columnName = columnName;
            this.message = message;
        }
    }

    /**
     * 执行 addValidationError 业务逻辑。
     *
     * @param rowNum 参数 rowNum。
     * @param columnName 参数 columnName。
     * @param message 参数 message。
     */
    public void addValidationError(int rowNum, String columnName, String message) {
        validationErrors.add(new ValidationError(rowNum, columnName, message));
    }

    /**
     * 执行 calculateValid 业务逻辑。
     */
    public void calculateValid() {
        this.valid = validationErrors.isEmpty();
    }
}

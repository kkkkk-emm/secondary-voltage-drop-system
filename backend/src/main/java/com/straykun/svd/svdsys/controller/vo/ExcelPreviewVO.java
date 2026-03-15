package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 预览结果 VO
 */
@Data
public class ExcelPreviewVO {

    /**
     * 总行数（不含表头）
     */
    private int totalRows;

    /**
     * 表头列表
     */
    private List<String> headers = new ArrayList<>();

    /**
     * 数据行（每行是一个 Map，key 为列名）
     */
    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 校验错误列表
     */
    private List<ValidationError> validationErrors = new ArrayList<>();

    /**
     * 是否校验通过（无错误可以导入）
     */
    private boolean valid;

    @Data
    public static class ValidationError {
        /**
         * 行号（Excel 中的行号，从 2 开始，1 为表头）
         */
        private int rowNum;

        /**
         * 列名
         */
        private String columnName;

        /**
         * 错误信息
         */
        private String message;

        public ValidationError(int rowNum, String columnName, String message) {
            this.rowNum = rowNum;
            this.columnName = columnName;
            this.message = message;
        }
    }

    public void addValidationError(int rowNum, String columnName, String message) {
        validationErrors.add(new ValidationError(rowNum, columnName, message));
    }

    /**
     * 计算是否校验通过
     */
    public void calculateValid() {
        this.valid = validationErrors.isEmpty();
    }
}

package com.straykun.svd.svdsys.controller.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入结果 VO
 */
@Data
public class ExcelImportResultVO {

    /**
     * 总行数（不含表头）
     */
    private int totalRows;

    /**
     * 成功导入行数
     */
    private int successRows;

    /**
     * 失败行数
     */
    private int failedRows;

    /**
     * 错误详情列表
     */
    private List<ErrorDetail> errors = new ArrayList<>();

    @Data
    public static class ErrorDetail {
        /**
         * 行号（Excel 中的行号，从 1 开始，含表头）
         */
        private int rowNum;

        /**
         * 错误信息
         */
        private String message;

        public ErrorDetail(int rowNum, String message) {
            this.rowNum = rowNum;
            this.message = message;
        }
    }

    public void addError(int rowNum, String message) {
        this.errors.add(new ErrorDetail(rowNum, message));
        this.failedRows++;
    }

    public void incrementSuccess() {
        this.successRows++;
    }
}

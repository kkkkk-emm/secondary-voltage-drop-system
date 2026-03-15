package com.straykun.svd.svdsys.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 简单分页结果封装，结构与前端/接口文档约定一致
 */
@Data
public class PageResult<T> {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 当前页数据
     */
    private List<T> records;

    public static <T> PageResult<T> of(long total, long size, long current, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(total);
        r.setSize(size);
        r.setCurrent(current);
        r.setRecords(records == null ? Collections.emptyList() : records);
        long pages = size <= 0 ? 0 : (total + size - 1) / size;
        r.setPages(pages);
        return r;
    }
}

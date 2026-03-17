package com.straykun.svd.svdsys.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果对象。
 */
@Data
public class PageResult<T> {

    private long total;

    private long size;

    private long current;

    private long pages;

    private List<T> records;

    /**
     * 执行 of 业务逻辑。
     *
     * @param total 参数 total。
     * @param size 参数 size。
     * @param current 参数 current。
     * @param records 参数 records。
     * @return 返回分页结果。
     */
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

package com.ise.platform.common.api;

import java.util.List;

public class PagedData<T> {

    private List<T> records;
    private int pageNo;
    private int pageSize;
    private long total;

    public PagedData(List<T> records, int pageNo, int pageSize, long total) {
        this.records = records;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }
}

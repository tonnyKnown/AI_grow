package com.oa.system.dto;

import java.util.List;

public class PageResponse<T> {
    private long total;
    private int pageNum;
    private int pageSize;
    private int totalPages;
    private List<T> records;

    public PageResponse() {
    }

    public PageResponse(long total, int pageNum, int pageSize, int totalPages, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public static <T> PageResponse<T> of(long total, int pageNum, int pageSize, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setRecords(records);
        if (pageSize > 0) {
            response.setTotalPages((int) Math.ceil((double) total / pageSize));
        }
        return response;
    }
}

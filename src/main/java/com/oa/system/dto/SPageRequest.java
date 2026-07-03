package com.oa.system.dto;

public class SPageRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String sortBy;
    private String sortOrder;
    private String startTime;
    private String endTime;

    public SPageRequest() {
    }

    public SPageRequest(Integer pageNum, Integer pageSize, String keyword, String sortBy, String sortOrder, String startTime, String endTime) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.keyword = keyword;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

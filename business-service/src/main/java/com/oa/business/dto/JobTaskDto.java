package com.oa.business.dto;

import java.util.Date;

public class JobTaskDto {
    
    private Long id;
    private String jobName;
    private String jobDesc;
    private String cron;
    private String jobHandler;
    private Integer status;
    private Integer shardingCount;
    private String jobParam;
    private String creator;
    private Date createTime;
    private Date updateTime;
    private String lastRunTime;
    private Integer runTimes;
    private Integer successTimes;
    private Integer failTimes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getJobHandler() {
        return jobHandler;
    }

    public void setJobHandler(String jobHandler) {
        this.jobHandler = jobHandler;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getShardingCount() {
        return shardingCount;
    }

    public void setShardingCount(Integer shardingCount) {
        this.shardingCount = shardingCount;
    }

    public String getJobParam() {
        return jobParam;
    }

    public void setJobParam(String jobParam) {
        this.jobParam = jobParam;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(String lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public Integer getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(Integer runTimes) {
        this.runTimes = runTimes;
    }

    public Integer getSuccessTimes() {
        return successTimes;
    }

    public void setSuccessTimes(Integer successTimes) {
        this.successTimes = successTimes;
    }

    public Integer getFailTimes() {
        return failTimes;
    }

    public void setFailTimes(Integer failTimes) {
        this.failTimes = failTimes;
    }
}
package com.oa.business.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Marketing {
    private Long id;
    private String name;
    private String type;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private String remark;
}

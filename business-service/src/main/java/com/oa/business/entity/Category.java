package com.oa.business.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}

package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Role {
    private Long id;
    private String roleName;
    private String roleKey;
    private String description;
    private Integer status = 1;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private String remark;
}

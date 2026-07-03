package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Permission {
    private Long id;
    private String permissionName;
    private String permissionKey;
    private String resourceType;
    private String path;
    private String component;
    private Long parentId;
    private Integer orderNum = 0;
    private Integer status = 1;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private List<Permission> children;
}

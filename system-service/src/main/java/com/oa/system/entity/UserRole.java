package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private Long createBy;
    private LocalDateTime createTime;
    private String roleName;
    private String roleKey;
}

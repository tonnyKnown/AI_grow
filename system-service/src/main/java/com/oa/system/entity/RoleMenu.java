package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleMenu {
    private Long id;
    private Long roleId;
    private Long menuId;
    private Long createBy;
    private LocalDateTime createTime;
}

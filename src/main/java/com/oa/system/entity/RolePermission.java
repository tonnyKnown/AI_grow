package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 * 
 * 用于建立角色与权限之间的多对多关系，一个角色可以拥有多个权限
 */
@Data
public class RolePermission {
    /**
     * 关联ID，主键，自增
     */
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 权限ID
     */
    private Long permissionId;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

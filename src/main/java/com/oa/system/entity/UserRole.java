package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 
 * 用于建立用户与角色之间的多对多关系，一个用户可以拥有多个角色
 */
@Data
public class UserRole {
    /**
     * 关联ID，主键，自增
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 角色名称（冗余字段，用于查询优化）
     */
    private String roleName;
    
    /**
     * 角色标识（冗余字段，用于查询优化）
     */
    private String roleKey;
}

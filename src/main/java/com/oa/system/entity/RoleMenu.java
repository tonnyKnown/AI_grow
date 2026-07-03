package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色菜单关联实体类
 * 
 * 用于建立角色与菜单之间的多对多关系，一个角色可以拥有多个菜单
 */
@Data
public class RoleMenu {
    /**
     * 关联ID，主键，自增
     */
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 菜单ID
     */
    private Long menuId;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

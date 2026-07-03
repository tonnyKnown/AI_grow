package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体类
 * 
 * 用于定义系统中的用户角色，角色关联权限，用户通过角色获得相应的访问权限
 */
@Data
public class Role {
    /**
     * 角色ID，主键，自增
     */
    private Long id;
    
    /**
     * 角色名称，用于显示
     */
    private String roleName;
    
    /**
     * 角色标识，用于权限控制，唯一
     */
    private String roleKey;
    
    /**
     * 角色描述信息
     */
    private String description;
    
    /**
     * 角色状态 0禁用 1启用
     */
    private Integer status = 1;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新人ID
     */
    private Long updateBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注信息
     */
    private String remark;
}

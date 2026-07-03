package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统权限实体类
 * 
 * 用于定义系统的访问权限资源，包括菜单、按钮、API等，支持树形结构
 */
@Data
public class Permission {
    /**
     * 权限ID，主键，自增
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限标识，用于权限控制，唯一
     */
    private String permissionKey;
    
    /**
     * 资源类型 menu菜单 button按钮 api接口
     */
    private String resourceType;
    
    /**
     * 前端路由路径
     */
    private String path;
    
    /**
     * 前端组件路径
     */
    private String component;
    
    /**
     * 父级权限ID，顶级权限为null
     */
    private Long parentId;
    
    /**
     * 排序号，数值越小越靠前
     */
    private Integer orderNum = 0;
    
    /**
     * 权限状态 0禁用 1启用
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
    
    /**
     * 子权限列表，用于树形结构展示
     */
    private List<Permission> children;
}

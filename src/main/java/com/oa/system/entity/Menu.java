package com.oa.system.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 系统菜单实体类
 * 
 * 用于定义系统的前端菜单，支持树形结构，菜单可以关联角色进行权限控制
 */
@Data
public class Menu {
    /**
     * 菜单ID，主键，自增
     */
    private Long id;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 前端路由路径
     */
    private String path;
    
    /**
     * 前端组件路径
     */
    private String component;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 父级菜单ID，顶级菜单为null
     */
    private Long parentId;
    
    /**
     * 排序号，数值越小越靠前
     */
    private Integer orderNum;
    
    /**
     * 可见角色标识列表，逗号分隔，为空表示所有角色可见
     */
    private String roleKeys;
    
    /**
     * 菜单状态 0禁用 1启用
     */
    private Integer status;
    
    /**
     * 创建人ID
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新人ID
     */
    private String updateBy;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 子菜单列表，用于树形结构展示
     */
    private List<Menu> children;
}

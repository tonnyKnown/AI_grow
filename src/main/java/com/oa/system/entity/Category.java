package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 * 
 * 用于定义商品的分类信息，支持树形结构的父子分类关系
 */
@Data
public class Category {
    /**
     * 分类ID，主键，自增
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类编码，唯一标识
     */
    private String categoryCode;
    
    /**
     * 排序号，数值越小越靠前
     */
    private Integer sort = 0;
    
    /**
     * 分类状态 0禁用 1启用
     */
    private Integer status = 1;
    
    /**
     * 父级分类ID，顶级分类为null
     */
    private Long parentId;
    
    /**
     * 备注信息
     */
    private String remark;
    
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
}

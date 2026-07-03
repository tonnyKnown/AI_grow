package com.oa.system.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 
 * 用于存储商品的基本信息，包括商品名称、编码、价格、库存、分类等
 */
@Data
public class Product {
    /**
     * 商品ID，主键，自增
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品编码，唯一标识
     */
    private String productCode;
    
    /**
     * 商品分类
     */
    private String category;
    
    /**
     * 商品价格，保留两位小数
     */
    private BigDecimal price;
    
    /**
     * 商品库存数量
     */
    private Integer stock;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品图片URL
     */
    private String imageUrl;
    
    /**
     * 商品状态 0下架 1上架
     */
    private Integer status = 1;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 更新人ID
     */
    private Long updateBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime = LocalDateTime.now();
    
    /**
     * 备注信息
     */
    private String remark;
}

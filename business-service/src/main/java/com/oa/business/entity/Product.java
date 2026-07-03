package com.oa.business.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String productName;
    private String productCode;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private String remark;
}

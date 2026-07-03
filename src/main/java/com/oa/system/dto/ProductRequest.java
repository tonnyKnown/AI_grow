package com.oa.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Long id;
    
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    
    private String productCode;
    
    private String category;
    
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    
    private Integer stock;
    
    private String description;
    
    private String imageUrl;
    
    private Integer status = 1;
    
    private String remark;
}

package com.oa.system.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;
    private String remark;
}

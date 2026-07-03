package com.oa.system.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * 用于存储订单信息，包括商品、数量、价格、收货地址、订单状态等
 */
@Data
public class Order {
    /**
     * 订单ID，主键，自增
     */
    private Long id;
    
    /**
     * 订单编号，唯一标识
     */
    private String orderNo;
    
    /**
     * 下单用户ID
     */
    private Long userId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 商品单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单状态 1待发货 2已发货 3配送中 4已收货 5已完成 6已取消 7退货中 8已退货 9已退款
     */
    private Integer status;
    
    /**
     * 收货地址
     */
    private String shippingAddress;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人手机号
     */
    private String receiverPhone;
    
    /**
     * 订单备注
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

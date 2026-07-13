package com.oa.business.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Express {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String expressCompany;
    private String expressNo;
    private Integer status;
    private String trackingNodes;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}

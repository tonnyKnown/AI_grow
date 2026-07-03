package com.oa.business.entity;

import lombok.Data;

@Data
public class DashboardStats {
    private Long totalUsers;
    private Long totalProducts;
    private Long totalOrders;
    private Long totalMarketing;
}

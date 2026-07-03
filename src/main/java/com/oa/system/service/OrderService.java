package com.oa.system.service;

import com.oa.system.dto.OrderRequest;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Order;

public interface OrderService {
    PageResponse<Order> getOrderPage(SPageRequest request);
    Order getOrderById(Long id);
    void createOrder(OrderRequest request, Long userId);
    void updateOrder(Long id, Integer quantity, String receiverName, String receiverPhone, String shippingAddress, String remark, Long userId);
    void updateOrderStatus(Long id, Integer status, Long userId);
    void deleteOrder(Long id, Long userId);
}

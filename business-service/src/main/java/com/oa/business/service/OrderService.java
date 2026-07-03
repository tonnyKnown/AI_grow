package com.oa.business.service;

import com.oa.business.entity.Order;
import com.oa.business.dto.PageResponse;

import java.util.List;

public interface OrderService {
    Order getOrderById(Long id);
    List<Order> getAllOrders();
    List<Order> getOrdersByCondition(String orderNo, String productName, Integer status);
    List<Order> getOrdersByUserIdOrderNoStatus(Long userId, String orderNo, Integer status);
    List<Order> getOrdersByUserIdAndStatus(Long userId, Integer status);
    PageResponse<Order> getOrdersPage(int pageNum, int pageSize);
    void createOrder(Order order, Long createBy);
    void updateOrder(Order order, Long updateBy);
    void updateOrderStatus(Order order, Long updateBy);
    void deleteOrder(Long id);

    Order getOrderByOrderId(String orderId);
}

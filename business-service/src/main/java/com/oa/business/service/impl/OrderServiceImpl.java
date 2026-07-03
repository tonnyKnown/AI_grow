package com.oa.business.service.impl;

import com.oa.business.dto.PageResponse;
import com.oa.business.entity.Order;
import com.oa.business.mapper.OrderMapper;
import com.oa.business.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
    }

    @Override
    public List<Order> getOrdersByCondition(String orderNo, String productName, Integer status) {
        return orderMapper.selectByCondition(orderNo, productName, status);
    }

    @Override
    public List<Order> getOrdersByUserIdOrderNoStatus(Long userId, String orderNo, Integer status) {
        return orderMapper.selectByUserIdOrderNoStatus(userId, orderNo, status);
    }

    @Override
    public List<Order> getOrdersByUserIdAndStatus(Long userId, Integer status) {
        return orderMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public PageResponse<Order> getOrdersPage(int pageNum, int pageSize) {
        List<Order> all = orderMapper.selectAll();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<Order> records = start < all.size() ? all.subList(start, end) : List.of();
        return PageResponse.of(all.size(), pageNum, pageSize, records);
    }

    @Override
    public void createOrder(Order order, Long createBy) {
        order.setCreateBy(createBy);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
    }

    @Override
    public void updateOrder(Order order, Long updateBy) {
        order.setUpdateBy(updateBy);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateOrder(order);
    }

    @Override
    public void updateOrderStatus(Order order, Long updateBy) {
        order.setUpdateBy(updateBy);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
    }

    @Override
    public void deleteOrder(Long id) {
        orderMapper.deleteById(id);
    }

    @Override
    public Order getOrderByOrderId(String orderId) {
        return orderMapper.selectByOrderNo(orderId);

    }
}

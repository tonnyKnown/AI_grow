package com.oa.system.service.impl;

import com.oa.system.dto.OrderRequest;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Order;
import com.oa.system.entity.Product;
import com.oa.system.mapper.OrderMapper;
import com.oa.system.mapper.ProductMapper;
import com.oa.system.service.OrderService;
import com.oa.system.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SmsService smsService;

    @Override
    public PageResponse<Order> getOrderPage(SPageRequest request) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        var orders = orderMapper.selectOrderPage(null, request.getKeyword(), offset, request.getPageSize());
        long total = orderMapper.countOrderPage(null, request.getKeyword());

        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), orders);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    @Override
    @Transactional
    public void createOrder(OrderRequest request, Long userId) {
        Product product = productMapper.selectById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId() != null ? request.getUserId() : 1L);
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus(1);
        order.setShippingAddress(request.getShippingAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setRemark(request.getRemark());
        order.setCreateBy(userId);
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        product.setStock(product.getStock() - request.getQuantity());
        product.setUpdateBy(userId);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);
    }

    @Override
    @Transactional
    public void updateOrder(Long id, Integer quantity, String receiverName, String receiverPhone, String shippingAddress, String remark, Long userId) {
        Order order = orderMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Product product = productMapper.selectById(order.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (quantity != order.getQuantity()) {
            int stockDiff = quantity - order.getQuantity();
            if (product.getStock() < stockDiff) {
                throw new RuntimeException("库存不足");
            }
            product.setStock(product.getStock() - stockDiff);
            product.setUpdateBy(userId);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.update(product);

            BigDecimal newTotalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            order.setQuantity(quantity);
            order.setTotalAmount(newTotalAmount);
        }

        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        order.setRemark(remark);
        order.setUpdateBy(userId);
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.update(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, Integer status, Long userId) {
        Order order = orderMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Integer oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdateBy(userId);
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.update(order);

        if (oldStatus != status && order.getReceiverPhone() != null && !order.getReceiverPhone().isEmpty()) {
            smsService.sendOrderStatusNotification(order.getReceiverPhone(), order.getOrderNo(), status);
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Long id, Long userId) {
        Order order = orderMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Product product = productMapper.selectById(order.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        product.setStock(product.getStock() + order.getQuantity());
        product.setUpdateBy(userId);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);

        orderMapper.deleteById(id);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.entity.Order;
import com.oa.business.service.OrderService;
import com.oa.business.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        List<Order> allOrders = orderService.getOrdersByCondition(orderNo, productName, status);
        int total = allOrders.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Order> records = start < total ? allOrders.subList(start, end) : List.of();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @PostMapping
    public Result<Void> createOrder(@RequestBody Order order,
                                    @RequestHeader(value = "userId") Long userId) {
        orderService.createOrder(order, userId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> updateOrder(@RequestBody Order order,
                                    @RequestHeader(value = "userId") Long userId) {
        orderService.updateOrder(order, userId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success("删除成功", null);
    }
}

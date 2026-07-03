package com.oa.business.controller;

import com.oa.business.aspect.ApiLog;
import com.oa.business.common.Result;
import com.oa.business.entity.Order;
import com.oa.business.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/business/agent")
public class AgentOrderController {
    private static final Logger log = LoggerFactory.getLogger(AgentOrderController.class);
    @Autowired
    private OrderService orderService;

    @ApiLog("查询订单列表")
    @GetMapping("/orders")
    public Result<Map<String, Object>> queryOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Order> orders = orderService.getOrdersByUserIdOrderNoStatus(userId, orderNo, status);

        int total = orders.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Order> records = start < total ? orders.subList(start, end) : List.of();

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    @ApiLog("根据订单ID查询订单详情")
    @GetMapping("/orders/{id}")
    public Result<Order> getOrderByOrderId(@PathVariable String id) {
        return Result.success(orderService.getOrderByOrderId(id));
    }

    @ApiLog("查询订单数量统计")
    @GetMapping("/orders/count")
    public Result<Map<String, Object>> getOrderCount(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        List<Order> orders = orderService.getOrdersByUserIdAndStatus(userId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("total", orders.size());
        data.put("status", status != null ? status : "all");

        return Result.success(data);
    }
}

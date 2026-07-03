package com.oa.system.controller;

import com.oa.system.dto.OrderRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.security.LoginUser;
import com.oa.system.service.OrderService;
import com.oa.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result getOrderPage(SPageRequest request) {
        return Result.success(orderService.getOrderPage(request));
    }

    @GetMapping("/{id}")
    public Result getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @PostMapping
    public Result createOrder(@RequestBody OrderRequest request) {
        Long userId = getCurrentUserId();
        orderService.createOrder(request, userId);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result updateOrder(@PathVariable Long id,
                              @RequestParam Integer quantity,
                              @RequestParam String receiverName,
                              @RequestParam String receiverPhone,
                              @RequestParam String shippingAddress,
                              @RequestParam(required = false) String remark) {
        Long userId = getCurrentUserId();
        orderService.updateOrder(id, quantity, receiverName, receiverPhone, shippingAddress, remark, userId);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/status")
    public Result updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long userId = getCurrentUserId();
        orderService.updateOrderStatus(id, status, userId);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.deleteOrder(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 从SecurityContext中获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return loginUser.getUserId();
        }
        return 1L; // 默认值，防止空指针
    }
}

package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Marketing;
import com.oa.system.security.LoginUser;
import com.oa.system.service.MarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营销活动控制器
 */
@RestController
@RequestMapping("/api/marketing")
@RequiredArgsConstructor
public class MarketingController {

    private final MarketingService marketingService;

    /**
     * 获取所有活动列表
     */
    @GetMapping("/list")
    public Result<List<Marketing>> getAllMarketing() {
        List<Marketing> marketingList = marketingService.getAllMarketing();
        return Result.success(marketingList);
    }

    /**
     * 分页查询活动列表
     */
    @GetMapping
    public Result<PageResponse<Marketing>> getMarketingPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {

        List<Marketing> list = marketingService.getMarketingPage(name, type, status, pageNum, pageSize);
        int total = marketingService.getMarketingCount(name, type, status);

        PageResponse<Marketing> response = PageResponse.of((long) total, pageNum, pageSize, list);

        return Result.success(response);
    }

    /**
     * 根据ID获取活动详情
     */
    @GetMapping("/{id}")
    public Result<Marketing> getMarketingById(@PathVariable Long id) {
        Marketing marketing = marketingService.getMarketingById(id);
        if (marketing == null) {
            return Result.error("活动不存在");
        }
        return Result.success(marketing);
    }

    /**
     * 创建营销活动
     */
    @PostMapping
    public Result<Marketing> createMarketing(@RequestBody Marketing marketing) {
        // 从token中获取当前登录用户信息
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null) {
            // createBy存储用户名，createByName存储真实姓名
            marketing.setCreateBy(loginUser.getUsername());
            marketing.setCreateByName(loginUser.getRealName() != null ? loginUser.getRealName() : loginUser.getUsername());
            marketing.setUpdateBy(loginUser.getUsername());
        }

        Marketing created = marketingService.createMarketing(marketing);
        return Result.success("创建成功", created);
    }

    /**
     * 更新营销活动
     */
    @PutMapping("/{id}")
    public Result<Marketing> updateMarketing(@PathVariable Long id, @RequestBody Marketing marketing) {
        marketing.setId(id);

        // 从token中获取当前登录用户信息
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null) {
            marketing.setUpdateBy(loginUser.getUsername());
        }

        Marketing updated = marketingService.updateMarketing(marketing);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除营销活动
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteMarketing(@PathVariable Long id) {
        Marketing marketing = marketingService.getMarketingById(id);
        if (marketing == null) {
            return Result.error("活动不存在");
        }
        marketingService.deleteMarketing(id);
        return Result.success("删除成功");
    }

    /**
     * 根据类型获取活动列表
     */
    @GetMapping("/type/{type}")
    public Result<List<Marketing>> getMarketingByType(@PathVariable String type) {
        List<Marketing> marketingList = marketingService.getMarketingByType(type);
        return Result.success(marketingList);
    }

    /**
     * 根据状态获取活动列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Marketing>> getMarketingByStatus(@PathVariable Integer status) {
        List<Marketing> marketingList = marketingService.getMarketingByStatus(status);
        return Result.success(marketingList);
    }

    /**
     * 从SecurityContext中获取当前登录用户信息
     */
    private LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }
}
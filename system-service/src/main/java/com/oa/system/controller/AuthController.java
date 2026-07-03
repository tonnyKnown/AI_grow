package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.*;
import com.oa.system.entity.Menu;
import com.oa.system.service.AuthService;
import com.oa.system.service.MenuService;
import com.oa.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/system/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.success("退出成功", null);
    }

    @GetMapping("/current")
    public Result<UserResponse> getCurrentUser(
            @RequestHeader(value = "userId", required = false) String userIdHeader,
            @RequestHeader(value = "X-Username", required = false) String usernameHeader) {
        try {
            log.info("从网关获取用户信息, userId: {}, username: {}", userIdHeader, usernameHeader);
            
            if (usernameHeader == null || usernameHeader.isEmpty()) {
                return Result.error(401, "未认证");
            }
            
            UserResponse user = userService.getUserByUsername(usernameHeader);
            log.info("获取用户信息: {}", user);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取当前用户失败", e);
            return Result.error(401, "获取用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/menu")
    public Result<List<Menu>> getMenu(
            @RequestHeader(value = "userId", required = false) String userIdHeader,
            @RequestHeader(value = "X-Username", required = false) String usernameHeader) {
        try {
            log.info("从网关获取用户信息, userId: {}, username: {}", userIdHeader, usernameHeader);
            
            if (usernameHeader == null || usernameHeader.isEmpty()) {
                return Result.success(Arrays.asList());
            }
            
            UserResponse user = userService.getUserByUsername(usernameHeader);
            if (user == null || user.getRoleNames() == null || user.getRoleNames().isEmpty()) {
                return Result.success(Arrays.asList());
            }
            String roles = String.join(",", user.getRoleNames());
            List<Menu> menus = menuService.getMenusByRoles(roles);
            return Result.success(menus);
        } catch (Exception e) {
            log.error("获取菜单失败", e);
            return Result.error(500, "获取菜单失败: " + e.getMessage());
        }
    }
}
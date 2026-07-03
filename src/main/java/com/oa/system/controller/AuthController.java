package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.LoginRequest;
import com.oa.system.dto.LoginResponse;
import com.oa.system.entity.Menu;
import com.oa.system.service.AuthService;
import com.oa.system.service.MenuService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private MenuService menuService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success("退出登录成功");
    }

    @GetMapping("/current")
    public Result<LoginResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
        LoginResponse response = authService.getCurrentUser(token);
        return Result.success(response);
    }

    @GetMapping("/menu")
    public Result<List<Menu>> getMenu(@RequestHeader("Authorization") String token) {
        LoginResponse user = authService.getCurrentUser(token);
        List<Menu> menus = menuService.getMenuByRoles(user.getRoles());
        return Result.success(menus);
    }
}

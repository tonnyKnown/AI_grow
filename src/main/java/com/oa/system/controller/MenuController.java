package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.entity.Menu;
import com.oa.system.security.LoginUser;
import com.oa.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/list")
    public Result<List<Menu>> getAllMenus() {
        List<Menu> menus = menuService.getAllMenus();
        return Result.success(menus);
    }

    @GetMapping("/roles")
    public Result<List<Menu>> getMenuByRoles(@RequestParam String roles) {
        List<String> roleList = Arrays.asList(roles.split(","));
        List<Menu> menus = menuService.getMenuByRoles(roleList);
        return Result.success(menus);
    }

    @GetMapping("/{id}")
    public Result<Menu> getMenuById(@PathVariable Long id) {
        Menu menu = menuService.getMenuById(id);
        return Result.success(menu);
    }

    @PostMapping
    public Result<Menu> createMenu(@RequestBody Menu menu) {
        // 从token中获取当前登录用户信息
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null) {
            menu.setCreateBy(String.valueOf(loginUser.getUserId()));
        }
        Menu created = menuService.createMenu(menu);
        return Result.success("创建成功", created);
    }

    @PutMapping("/{id}")
    public Result<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        menu.setId(id);
        // 从token中获取当前登录用户信息
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null) {
            menu.setUpdateBy(String.valueOf(loginUser.getUserId()));
        }
        Menu updated = menuService.updateMenu(menu);
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteMenu(@PathVariable Long id) {
        // 删除操作也记录操作人（可选）
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null) {
            // 如果需要记录删除操作人，可以在这里处理
        }
        menuService.deleteMenu(id);
        return Result.success("删除成功");
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
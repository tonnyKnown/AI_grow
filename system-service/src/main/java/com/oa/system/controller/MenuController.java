package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.entity.Menu;
import com.oa.system.service.MenuService;
import com.oa.system.util.PermissionCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private PermissionCheckUtil permissionCheckUtil;

    @GetMapping("/list")
    public Result<List<Menu>> getAllMenus() {
        return Result.success(menuService.getAllMenus());
    }

    @GetMapping("/tree")
    public Result<List<Menu>> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }
    @GetMapping("/roles")
    public Result<List<Menu>> getMenusByRoles(@RequestParam String roles) {
        return Result.success(menuService.getMenusByRoles(roles));
    }

    @GetMapping("/{id}")
    public Result<Menu> getMenuById(@PathVariable Long id) {
        return Result.success(menuService.getMenuById(id));
    }

    @PostMapping
    public Result<Void> createMenu(@RequestBody Menu menu,
                                   @RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestHeader(value = "userId") Long userId) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "menu:manage")) {
            return Result.error("无权创建菜单，请联系管理员");
        }
        menuService.createMenu(menu, userId);
        return Result.success("创建成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody Menu menu,
                                   @RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestHeader(value = "userId") Long userId) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "menu:manage")) {
            return Result.error("无权更新菜单，请联系管理员");
        }
        menuService.updateMenu(menu, userId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "menu:manage")) {
            return Result.error("无权删除菜单，请联系管理员");
        }
        menuService.deleteMenu(id);
        return Result.success("删除成功", null);
    }
}

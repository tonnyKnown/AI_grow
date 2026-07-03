package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Permission;
import com.oa.system.service.PermissionService;
import com.oa.system.util.PermissionCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionCheckUtil permissionCheckUtil;

    @GetMapping
    public Result<PageResponse<Permission>> getPermissionsPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(permissionService.getPermissionsPage(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Permission>> getAllPermissions() {
        return Result.success(permissionService.getAllPermissions());
    }

    @GetMapping("/menu")
    public Result<List<Permission>> getMenuTree() {
        return Result.success(permissionService.getPermissionTree());
    }

    @GetMapping("/{id}")
    public Result<Permission> getPermissionById(@PathVariable Long id) {
        return Result.success(permissionService.getPermissionById(id));
    }

    @PostMapping
    public Result<Void> createPermission(@RequestBody Permission permission,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "permission:manage")) {
            return Result.error("无权创建权限，请联系管理员");
        }
        permissionService.createPermission(permission, "admin");
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> updatePermission(@RequestBody Permission permission,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "permission:manage")) {
            return Result.error("无权更新权限，请联系管理员");
        }
        permissionService.updatePermission(permission, "admin");
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "permission:manage")) {
            return Result.error("无权删除权限，请联系管理员");
        }
        permissionService.deletePermission(id);
        return Result.success("删除成功", null);
    }
}

package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Role;
import com.oa.system.service.RoleService;
import com.oa.system.util.PermissionCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionCheckUtil permissionCheckUtil;

    @GetMapping
    public Result<PageResponse<Role>> getRolesPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(roleService.getRolesPage(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        return Result.success(roleService.getRoleById(id));
    }

    @GetMapping("/all")
    public Result<java.util.List<Role>> getAllRoles() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}/permissions")
    public Result<java.util.List<Long>> getRolePermissionIds(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }

    @GetMapping("/permissions/tree")
    public Result<java.util.List<com.oa.system.entity.Permission>> getPermissionTree() {
        return Result.success(roleService.getPermissionTree());
    }

    @PostMapping
    public Result<Void> createRole(@RequestBody Role role,
                                   @RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestHeader(value = "userId", required = false) String userIdStr) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "role:manage")) {
            return Result.error("无权创建角色，请联系管理员");
        }
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 1L;
        roleService.createRole(role, userId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> updateRole(@RequestBody Role role,
                                   @RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestHeader(value = "userId", required = false) String userIdStr) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "role:manage")) {
            return Result.error("无权更新角色，请联系管理员");
        }
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 1L;
        roleService.updateRole(role, userId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "role:manage")) {
            return Result.error("无权删除角色，请联系管理员");
        }
        roleService.deleteRole(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody java.util.List<Long> permissionIds,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        // 权限校验
        if (token != null && !permissionCheckUtil.hasPermission(token, "role:manage")) {
            return Result.error("无权分配权限，请联系管理员");
        }
        roleService.assignPermissions(id, permissionIds);
        return Result.success("分配权限成功", null);
    }
}

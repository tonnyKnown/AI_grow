package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.RoleRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Permission;
import com.oa.system.entity.Role;
import com.oa.system.entity.RolePermission;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.service.RoleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    @Resource
    private  RoleService roleService;
    
    @Resource
    private RolePermissionMapper rolePermissionMapper;
    
    @Resource
    private PermissionMapper permissionMapper;
    
    @GetMapping
    public Result<PageResponse<Role>> getRolePage(SPageRequest request) {
        PageResponse<Role> response = roleService.getRolePage(request);
        return Result.success(response);
    }
    
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        Role response = roleService.getRoleById(id);
        return Result.success(response);
    }
    
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissionIds(@PathVariable Long id) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectByRoleId(id);
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        return Result.success(permissionIds);
    }
    
    @GetMapping("/permissions/tree")
    public Result<List<Permission>> getPermissionTree() {
        List<Permission> allPermissions = permissionMapper.selectByStatus(1);
        List<Permission> tree = buildPermissionTree(allPermissions);
        return Result.success(tree);
    }
    
    @PostMapping
    public Result<String> createRole(@Valid @RequestBody RoleRequest request) {
        roleService.createRole(request);
        return Result.success("角色创建成功");
    }
    
    @PutMapping
    public Result<String> updateRole(@Valid @RequestBody RoleRequest request) {
        roleService.updateRole(request);
        return Result.success("角色更新成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("角色删除成功");
    }
    
    @PostMapping("/{id}/permissions")
    public Result<String> assignPermissions(@PathVariable Long id, @RequestBody Long[] permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success("权限分配成功");
    }
    
    private List<Permission> buildPermissionTree(List<Permission> permissions) {
        List<Permission> tree = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission.getParentId() == null || permission.getParentId() == 0) {
                permission.setChildren(getChildren(permission, permissions));
                tree.add(permission);
            }
        }
        return tree;
    }
    
    private List<Permission> getChildren(Permission parent, List<Permission> permissions) {
        List<Permission> children = new ArrayList<>();
        for (Permission permission : permissions) {
            if (parent.getId().equals(permission.getParentId())) {
                permission.setChildren(getChildren(permission, permissions));
                children.add(permission);
            }
        }
        return children;
    }
}

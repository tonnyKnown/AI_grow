package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.PermissionRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Permission;
import com.oa.system.service.PermissionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    @Resource
    private PermissionService permissionService;
    
    @GetMapping
    public Result<PageResponse<Permission>> getPermissionPage(SPageRequest request) {
        PageResponse<Permission> response = permissionService.getPermissionPage(request);
        return Result.success(response);
    }
    
    @GetMapping("/{id}")
    public Result<Permission> getPermissionById(@PathVariable Long id) {
        Permission response = permissionService.getPermissionById(id);
        return Result.success(response);
    }
    
    @GetMapping("/all")
    public Result<List<Permission>> getAllPermissions() {
        List<Permission> response = permissionService.getAllPermissions();
        return Result.success(response);
    }
    
    @GetMapping("/menu")
    public Result<List<Permission>> getMenuTree() {
        List<Permission> response = permissionService.getMenuTree();
        return Result.success(response);
    }
    
    @PostMapping
    public Result<String> createPermission(@Valid @RequestBody PermissionRequest request) {
        permissionService.createPermission(request);
        return Result.success("权限创建成功");
    }
    
    @PutMapping
    public Result<String> updatePermission(@Valid @RequestBody PermissionRequest request) {
        permissionService.updatePermission(request);
        return Result.success("权限更新成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success("权限删除成功");
    }
}
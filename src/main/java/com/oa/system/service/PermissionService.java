package com.oa.system.service;

import com.oa.system.dto.PageResponse;
import com.oa.system.dto.PermissionRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Permission;

import java.util.List;

public interface PermissionService {
    PageResponse<Permission> getPermissionPage(SPageRequest request);
    Permission getPermissionById(Long id);
    List<Permission> getAllPermissions();
    List<Permission> getMenuTree();
    void createPermission(PermissionRequest request);
    void updatePermission(PermissionRequest request);
    void deletePermission(Long id);
}
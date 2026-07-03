package com.oa.system.service;

import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getAllPermissions();
    PageResponse<Permission> getPermissionsPage(int pageNum, int pageSize);
    Permission getPermissionById(Long id);
    List<Permission> getPermissionTree();
    void createPermission(Permission permission, String createBy);
    void updatePermission(Permission permission, String updateBy);
    void deletePermission(Long id);
}

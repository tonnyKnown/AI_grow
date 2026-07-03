package com.oa.system.service.impl;

import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Permission;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectAll();
    }

    @Override
    public PageResponse<Permission> getPermissionsPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Permission> records = permissionMapper.selectPage(offset, pageSize);
        long total = permissionMapper.count();
        return PageResponse.of(total, pageNum, pageSize, records);
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionMapper.selectById(id);
    }

    @Override
    public List<Permission> getPermissionTree() {
        return buildTree(permissionMapper.selectAll());
    }

    @Override
    public void createPermission(Permission permission, String createBy) {
        permission.setCreateBy(1L);
        permission.setCreateTime(LocalDateTime.now());
        permissionMapper.insert(permission);
    }

    @Override
    public void updatePermission(Permission permission, String updateBy) {
        permission.setUpdateBy(1L);
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.update(permission);
    }

    @Override
    public void deletePermission(Long id) {
        permissionMapper.deleteById(id);
    }

    private List<Permission> buildTree(List<Permission> permissions) {
        List<Permission> rootMenus = new ArrayList<>();
        for (Permission p : permissions) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                rootMenus.add(p);
            }
        }
        for (Permission p : rootMenus) {
            p.setChildren(getChildren(p.getId(), permissions));
        }
        return rootMenus;
    }

    private List<Permission> getChildren(Long parentId, List<Permission> allPermissions) {
        List<Permission> children = new ArrayList<>();
        for (Permission p : allPermissions) {
            if (p.getParentId() != null && p.getParentId().equals(parentId)) {
                p.setChildren(getChildren(p.getId(), allPermissions));
                children.add(p);
            }
        }
        return children;
    }
}

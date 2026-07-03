package com.oa.system.service.impl;

import com.oa.system.dto.PageResponse;
import com.oa.system.dto.PermissionRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Permission;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public PageResponse<Permission> getPermissionPage(SPageRequest request) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        List<Permission> permissions = permissionMapper.selectPermissionPage(offset, request.getPageSize());
        long total = permissionMapper.countPermissionPage();

        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), permissions);
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectByStatus(1);
    }

    @Override
    public List<Permission> getMenuTree() {
        List<Permission> all = permissionMapper.selectByStatus(1);
        return buildTree(all, 0L);
    }

    @Override
    @Transactional
    public void createPermission(PermissionRequest request) {
        Permission permission = new Permission();
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionKey(request.getPermissionKey());
        permission.setResourceType(request.getResourceType());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        permission.setOrderNum(request.getOrderNum() != null ? request.getOrderNum() : 0);
        permission.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        permission.setCreateBy(1L);
        permission.setCreateTime(LocalDateTime.now());
        permission.setRemark(request.getRemark());

        permissionMapper.insert(permission);
    }

    @Override
    @Transactional
    public void updatePermission(PermissionRequest request) {
        Permission permission = permissionMapper.selectById(request.getId())
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionKey(request.getPermissionKey());
        permission.setResourceType(request.getResourceType());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        permission.setOrderNum(request.getOrderNum() != null ? request.getOrderNum() : 0);
        permission.setStatus(request.getStatus());
        permission.setUpdateBy(1L);
        permission.setUpdateTime(LocalDateTime.now());
        permission.setRemark(request.getRemark());

        permissionMapper.update(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        rolePermissionMapper.deleteByPermissionId(id);
        permissionMapper.deleteById(id);
    }

    private List<Permission> buildTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> p.getParentId().equals(parentId))
                .peek(p -> p.setChildren(buildTree(permissions, p.getId())))
                .collect(Collectors.toList());
    }
}
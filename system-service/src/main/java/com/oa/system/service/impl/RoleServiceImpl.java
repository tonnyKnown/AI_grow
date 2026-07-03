package com.oa.system.service.impl;

import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Permission;
import com.oa.system.entity.Role;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Role getRoleById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectAll();
    }

    @Override
    public PageResponse<Role> getRolesPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Role> records = roleMapper.selectPage(offset, pageSize);
        long total = roleMapper.count();
        return PageResponse.of(total, pageNum, pageSize, records);
    }

    @Override
    public void createRole(Role role, Long createBy) {
        role.setCreateBy(createBy);
        role.setCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(Role role, Long updateBy) {
        role.setUpdateBy(updateBy);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.update(role);
    }

    @Override
    public void deleteRole(Long id) {
        roleMapper.deleteById(id);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<Permission> getPermissionTree() {
        return buildTree(permissionMapper.selectAll());
    }

    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolePermissionMapper.insertBatch(roleId, permissionIds);
        }
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

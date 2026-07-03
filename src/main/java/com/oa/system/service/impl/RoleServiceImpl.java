package com.oa.system.service.impl;

import com.oa.system.dto.PageResponse;
import com.oa.system.dto.RoleRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Role;
import com.oa.system.entity.RolePermission;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public PageResponse<Role> getRolePage(SPageRequest request) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        List<Role> roles = roleMapper.selectRolePage(offset, request.getPageSize());
        long total = roleMapper.countRolePage();

        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), roles);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
    }

    @Override
    @Transactional
    public void createRole(RoleRequest request) {
        if (roleMapper.existsByRoleKey(request.getRoleKey())) {
            throw new RuntimeException("角色标识已存在");
        }

        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setRoleKey(request.getRoleKey());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setCreateBy(1L);
        role.setCreateTime(LocalDateTime.now());
        role.setRemark(request.getRemark());

        roleMapper.insert(role);

        if (request.getPermissionIds() != null && request.getPermissionIds().length > 0) {
            for (Long permissionId : request.getPermissionIds()) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permissionId);
                rp.setCreateBy(1L);
                rp.setCreateTime(LocalDateTime.now());
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional
    public void updateRole(RoleRequest request) {
        Role role = roleMapper.selectById(request.getId())
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setUpdateBy(1L);
        role.setUpdateTime(LocalDateTime.now());
        role.setRemark(request.getRemark());

        roleMapper.update(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        rolePermissionMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, Long[] permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);

        if (permissionIds != null && permissionIds.length > 0) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreateBy(1L);
                rp.setCreateTime(LocalDateTime.now());
                rolePermissionMapper.insert(rp);
            }
        }
    }
}

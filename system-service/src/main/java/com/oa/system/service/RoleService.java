package com.oa.system.service;

import com.oa.system.dto.PageResponse;
import com.oa.system.entity.Permission;
import com.oa.system.entity.Role;

import java.util.List;

public interface RoleService {
    Role getRoleById(Long id);
    List<Role> getAllRoles();
    PageResponse<Role> getRolesPage(int pageNum, int pageSize);
    void createRole(Role role, Long createBy);
    void updateRole(Role role, Long updateBy);
    void deleteRole(Long id);
    List<Long> getRolePermissionIds(Long roleId);
    List<Permission> getPermissionTree();
    void assignPermissions(Long roleId, List<Long> permissionIds);
}

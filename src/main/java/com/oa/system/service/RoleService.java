package com.oa.system.service;

import com.oa.system.dto.*;
import com.oa.system.entity.Role;

public interface RoleService {
    PageResponse<Role> getRolePage(SPageRequest request);
    Role getRoleById(Long id);
    void createRole(RoleRequest request);
    void updateRole(RoleRequest request);
    void deleteRole(Long id);
    void assignPermissions(Long roleId, Long[] permissionIds);
}

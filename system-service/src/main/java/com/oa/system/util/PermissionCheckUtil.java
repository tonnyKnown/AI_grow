package com.oa.system.util;

import com.oa.system.entity.Permission;
import com.oa.system.entity.Role;
import com.oa.system.entity.User;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.mapper.UserMapper;
import com.oa.system.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限校验工具类
 *
 * <p>权限一律按「用户 → 角色 → 权限」链路从数据库解析。
 * 注意：不能用「用户名等于 admin」来判断超级管理员——用户名与角色无关，
 * 否则普通账号只要取名叫 admin 就能拿到全部权限，而真正的管理员账号（用户名任意）
 * 反而会被拒绝。</p>
 */
@Component
public class PermissionCheckUtil {

    /** 超级管理员角色标识（sys_role.role_key），拥有全部权限 */
    private static final String SUPER_ADMIN_ROLE_KEY = "admin";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取 token 对应用户的角色标识（role_key）集合
     */
    public Set<String> getRoleKeysFromToken(String token) {
        Set<String> roleKeys = new HashSet<>();
        User user = getUserFromToken(token);
        if (user == null) {
            return roleKeys;
        }
        for (Long roleId : userRoleMapper.selectRoleIdsByUserId(user.getId())) {
            Role role = roleMapper.selectById(roleId);
            if (role != null && role.getRoleKey() != null) {
                roleKeys.add(role.getRoleKey());
            }
        }
        return roleKeys;
    }

    /**
     * 从 token 中解析并获取用户权限列表
     */
    public Set<String> getPermissionsFromToken(String token) {
        Set<String> permissions = new HashSet<>();
        User user = getUserFromToken(token);
        if (user == null) {
            return permissions;
        }

        // 超级管理员角色拥有全部已定义权限
        if (getRoleKeysFromToken(token).contains(SUPER_ADMIN_ROLE_KEY)) {
            for (Permission permission : permissionMapper.selectAll()) {
                if (permission != null && permission.getPermissionKey() != null) {
                    permissions.add(permission.getPermissionKey());
                }
            }
            return permissions;
        }

        // 普通用户：汇总其所有角色关联的权限
        for (Long roleId : userRoleMapper.selectRoleIdsByUserId(user.getId())) {
            for (Long permissionId : rolePermissionMapper.selectPermissionIdsByRoleId(roleId)) {
                Permission permission = permissionMapper.selectById(permissionId);
                if (permission != null && permission.getPermissionKey() != null) {
                    permissions.add(permission.getPermissionKey());
                }
            }
        }
        return permissions;
    }

    /**
     * 检查用户是否拥有指定权限
     */
    public boolean hasPermission(String token, String permission) {
        // 超级管理员角色直接放行，避免受权限数据是否配置完整的影响
        if (getRoleKeysFromToken(token).contains(SUPER_ADMIN_ROLE_KEY)) {
            return true;
        }
        return getPermissionsFromToken(token).contains(permission);
    }

    /**
     * 检查用户是否拥有指定角色
     */
    public boolean hasRole(String token, String role) {
        return getRoleKeysFromToken(token).contains(role);
    }

    private User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || username.isEmpty()) {
            return null;
        }
        return userMapper.selectByUsername(username).orElse(null);
    }
}

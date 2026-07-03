package com.oa.system.util;

import com.oa.system.entity.User;
import com.oa.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限校验工具类
 */
@Component
public class PermissionCheckUtil {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从token中解析并获取用户权限列表
     */
    public Set<String> getPermissionsFromToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null) {
            return new HashSet<>();
        }

        User user = userMapper.selectByUsername(username).orElse(null);
        if (user == null) {
            return new HashSet<>();
        }

        // 获取用户角色
        Set<String> permissions = new HashSet<>();

        // 通过用户名查询权限（这里简化处理，实际应该通过角色关联查询）
        // 管理员角色拥有所有权限
        if ("admin".equals(username)) {
            permissions.add("role:manage");
            permissions.add("permission:manage");
            permissions.add("menu:manage");
            permissions.add("user:manage");
        }

        return permissions;
    }

    /**
     * 检查用户是否拥有指定权限
     */
    public boolean hasPermission(String token, String permission) {
        // admin 拥有所有权限
        String username = jwtUtil.getUsernameFromToken(token);
        if ("admin".equals(username)) {
            return true;
        }

        Set<String> permissions = getPermissionsFromToken(token);
        return permissions.contains(permission);
    }

    /**
     * 检查用户是否拥有指定角色
     */
    public boolean hasRole(String token, String role) {
        String username = jwtUtil.getUsernameFromToken(token);
        return role.equals(username);
    }
}

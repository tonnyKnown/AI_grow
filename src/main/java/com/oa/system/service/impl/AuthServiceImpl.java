package com.oa.system.service.impl;

import com.oa.system.dto.LoginRequest;
import com.oa.system.dto.LoginResponse;
import com.oa.system.entity.Permission;
import com.oa.system.entity.Role;
import com.oa.system.entity.User;
import com.oa.system.entity.UserRole;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.mapper.UserMapper;
import com.oa.system.mapper.UserRoleMapper;
import com.oa.system.security.JwtTokenProvider;
import com.oa.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userMapper.selectByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        redisTemplate.opsForValue().set("token:" + user.getUsername(), token, 24, TimeUnit.HOURS);

        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        response.setExpiresIn(tokenProvider.getExpiration());
        response.setRoles(roles);
        response.setPermissions(permissions);

        return response;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = tokenProvider.getUsernameFromToken(token);
        redisTemplate.delete("token:" + username);
    }

    @Override
    public LoginResponse getCurrentUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = tokenProvider.getUsernameFromToken(token);
        User user = userMapper.selectByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        response.setExpiresIn(tokenProvider.getExpiration());
        response.setRoles(roles);
        response.setPermissions(permissions);

        return response;
    }

    private List<String> getUserRoles(Long userId) {
        List<String> roleKeys = new ArrayList<>();
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<Role> roles = roleMapper.selectByIds(roleIds);
            for (Role role : roles) {
                roleKeys.add(role.getRoleKey());
            }
        }
        return roleKeys;
    }

    private List<String> getUserPermissions(Long userId) {
        List<String> permissionKeys = new ArrayList<>();
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            List<Permission> permissions = permissionMapper.selectByRoleIds(roleIds);
            for (Permission permission : permissions) {
                permissionKeys.add(permission.getPermissionKey());
            }
        }
        return permissionKeys;
    }
}

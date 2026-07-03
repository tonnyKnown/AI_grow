package com.oa.system.service.impl;

import com.oa.system.dto.LoginRequest;
import com.oa.system.dto.LoginResponse;
import com.oa.system.entity.User;
import com.oa.system.mapper.UserMapper;
import com.oa.system.mapper.UserRoleMapper;
import com.oa.system.mapper.RolePermissionMapper;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.service.AuthService;
import com.oa.system.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        LoginResponse response = new LoginResponse();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());

        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());
        response.setRoles(roles);
        response.setPermissions(permissions);

        return response;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("logout时token为空");
            return;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("无效的token: {}", token);
            return;
        }

        Date expiration = jwtUtil.getExpirationFromToken(token);
        if (expiration != null) {
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.MILLISECONDS);
                log.info("Token已加入黑名单, 剩余有效期: {}ms", ttl);
            }
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        log.info("从网关传递的token: {}", token);
        return "";
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(blacklistKey);
        return Boolean.TRUE.equals(exists);
    }

    private List<String> getUserRoles(Long userId) {
        try {
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
            if (roleIds.isEmpty()) {
                return new ArrayList<>();
            }
            return roleIds.stream()
                    .map(roleMapper::selectById)
                    .filter(r -> r != null)
                    .map(r -> r.getRoleKey())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户角色失败", e);
            return new ArrayList<>();
        }
    }

    private List<String> getUserPermissions(Long userId) {
        try {
            List<String> permissions = new ArrayList<>();
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
            for (Long roleId : roleIds) {
                List<Long> permIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
                for (Long permId : permIds) {
                    var permission = permissionMapper.selectById(permId);
                    if (permission != null && permission.getPermissionKey() != null) {
                        permissions.add(permission.getPermissionKey());
                    }
                }
            }
            return permissions;
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return new ArrayList<>();
        }
    }
}

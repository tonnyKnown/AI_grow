package com.oa.system.service.impl;

import com.oa.system.dto.*;
import com.oa.system.entity.Permission;
import com.oa.system.entity.User;
import com.oa.system.entity.UserRole;
import com.oa.system.mapper.PermissionMapper;
import com.oa.system.mapper.UserMapper;
import com.oa.system.mapper.UserRoleMapper;
import com.oa.system.service.UserService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<UserResponse> getUserPage(SPageRequest request) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        List<User> users = userMapper.selectUserPage(request.getKeyword(), offset, request.getPageSize());
        long total = userMapper.countUserPage(request.getKeyword());

        List<UserResponse> records = new ArrayList<>();
        for (User user : users) {
            records.add(toResponse(user));
        }

        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), records);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public void createUser(UserRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (StringUtils.isNotEmpty(request.getEmail()) && userMapper.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        if (request.getRoleIds() == null || request.getRoleIds().length == 0) {
            throw new RuntimeException("请至少选择一个角色");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setRemark(request.getRemark());
        user.setCreateBy(1L);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);

        if (request.getRoleIds() != null && request.getRoleIds().length > 0) {
            for (Long roleId : request.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRole.setCreateBy(1L);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional
    public void updateUser(UserRequest request) {
        User user = userMapper.selectById(request.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark());
        user.setUpdateBy(1L);
        user.setUpdateTime(LocalDateTime.now());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.update(user);

        if (request.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(request.getId());
            for (Long roleId : request.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(request.getId());
                userRole.setRoleId(roleId);
                userRole.setCreateBy(1L);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.deleteByUserId(id);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateBy(id);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = userMapper.selectById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        userRoleMapper.deleteByUserId(userId);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateBy(1L);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public PageResponse<UserResponse> getUserByRealName(String realName) {
        List<User> users = userMapper.selectByRealName(realName);
        
        List<UserResponse> records = new ArrayList<>();
        for (User user : users) {
            records.add(toResponse(user));
        }

        return PageResponse.of((long) records.size(), 1, 10, records);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        response.setRemark(user.getRemark());

        List<UserRole> userRoles = userRoleMapper.selectByUserIdWithRole(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<String> roleNames = userRoles.stream().map(UserRole::getRoleName).collect(Collectors.toList());
        
        response.setRoleIds(roleIds);
        response.setRoleNames(roleNames);
        
        List<String> permissions = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<Permission> permissionList = permissionMapper.selectByRoleIds(roleIds);
            permissions = permissionList.stream().map(Permission::getPermissionKey).collect(Collectors.toList());
        }
        response.setPermissions(permissions);

        return response;
    }
}

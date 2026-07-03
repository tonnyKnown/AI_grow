package com.oa.system.service.impl;

import com.oa.system.dto.*;
import com.oa.system.entity.User;
import com.oa.system.entity.UserRole;
import com.oa.system.entity.Role;
import com.oa.system.mapper.UserMapper;
import com.oa.system.mapper.UserRoleMapper;
import com.oa.system.mapper.RoleMapper;
import com.oa.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private RoleMapper roleMapper;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        return toResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username).orElse(null);
        return toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getUserByRealName(String realName) {
        List<User> users = userMapper.selectByRealName(realName);
        List<UserResponse> records = users.stream().map(this::toResponse).collect(Collectors.toList());
        return PageResponse.of(records.size(), 1, 10, records);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userMapper.selectAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void createUser(UserRequest request, Long createBy) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setCreateBy(createBy);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public void updateUser(UserRequest request, Long updateBy) {
        User user = userMapper.selectById(request.getId());
        if (user != null) {
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRealName(request.getRealName());
            user.setStatus(request.getStatus());
            user.setUpdateBy(updateBy);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.update(user);
        }
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        for (Long roleId : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreateBy(userId);
            userRoleMapper.insert(ur);
        }
    }

    private UserResponse toResponse(User user) {
        if (user == null) return null;
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

        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        response.setRoleIds(roleIds);

        List<String> roleNames = new ArrayList<>();
        for (Long roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role != null) {
                roleNames.add(role.getRoleName());
            }
        }
        response.setRoleNames(roleNames);
        response.setPermissions(new ArrayList<>());

        return response;
    }
}

package com.oa.system.service;

import com.oa.system.dto.UserRequest;
import com.oa.system.dto.UserResponse;
import com.oa.system.dto.PageResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
    PageResponse<UserResponse> getUserByRealName(String realName);
    List<UserResponse> getAllUsers();
    void createUser(UserRequest request, Long createBy);
    void updateUser(UserRequest request, Long updateBy);
    void deleteUser(Long id);
    void assignRoles(Long userId, List<Long> roleIds);
}

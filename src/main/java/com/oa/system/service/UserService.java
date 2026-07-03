package com.oa.system.service;

import com.oa.system.dto.*;

import java.util.List;

public interface UserService {
    PageResponse<UserResponse> getUserPage(SPageRequest request);
    UserResponse getUserById(Long id);
    void createUser(UserRequest request);
    void updateUser(UserRequest request);
    void deleteUser(Long id);
    void updatePassword(Long id, String oldPassword, String newPassword);
    void assignRoles(Long userId, List<Long> roleIds);
    PageResponse<UserResponse> getUserByRealName(String realName);
}

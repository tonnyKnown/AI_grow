package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.*;
import com.oa.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public Result<UserResponse> getUserByUsername(@PathVariable String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    @GetMapping("/getUserByIName")
    public Result<PageResponse<UserResponse>> getUserByIName(@RequestParam String userName) {
        return Result.success(userService.getUserByRealName(userName));
    }

    @GetMapping
    public Result<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<UserResponse> allUsers = userService.getAllUsers();
        int total = allUsers.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<UserResponse> records = start < total ? allUsers.subList(start, end) : List.of();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @PostMapping
    public Result<Void> createUser(@RequestBody UserRequest request,
                                    @RequestHeader(value = "userId", required = false) String userIdStr) {
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 1L;
        userService.createUser(request, userId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> updateUser(@RequestBody UserRequest request,
                                    @RequestHeader(value = "userId", required = false) String userIdStr) {
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : 1L;
        userService.updateUser(request, userId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{userId}/roles")
    public Result<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return Result.success("分配角色成功", null);
    }
}

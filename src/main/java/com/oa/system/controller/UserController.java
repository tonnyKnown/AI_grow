package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.*;
import com.oa.system.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping
    public Result<PageResponse<UserResponse>> getUserPage(SPageRequest request) {
        PageResponse<UserResponse> response = userService.getUserPage(request);
        return Result.success(response);
    }
    
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return Result.success(response);
    }

    @GetMapping("/getUserByIName")
    public Result<PageResponse<UserResponse>> getUserByIName(@RequestParam String userName) {
        PageResponse<UserResponse> response = userService.getUserByRealName(userName);
        return Result.success("操作成功", response);
    }
    
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserRequest request) {
        userService.createUser(request);
        return Result.success("用户创建成功");
    }
    
    @PutMapping
    public Result<String> updateUser(@Valid @RequestBody UserRequest request) {
        userService.updateUser(request);
        return Result.success("用户更新成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功");
    }
    
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestParam Long id,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userService.updatePassword(id, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    @PostMapping("/{id}/roles")
    public Result<String> assignRoles(@PathVariable Long id, @RequestBody Long[] roleIds) {
        List<Long> roleIdList = Arrays.stream(roleIds).collect(Collectors.toList());
        userService.assignRoles(id, roleIdList);
        return Result.success("角色分配成功");
    }
}

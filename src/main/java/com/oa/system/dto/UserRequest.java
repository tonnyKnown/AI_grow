package com.oa.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    private String avatar;
    
    private Integer status = 1;
    
    private String remark;
    
    private Long[] roleIds;

    public UserRequest() {}

    public UserRequest(Long id, String username, String password, String email, String phone, 
                      String realName, String avatar, Integer status, String remark, Long[] roleIds) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.realName = realName;
        this.avatar = avatar;
        this.status = status;
        this.remark = remark;
        this.roleIds = roleIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds) {
        this.roleIds = roleIds;
    }
}
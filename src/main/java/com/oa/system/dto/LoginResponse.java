package com.oa.system.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private Long expiresIn;
    private List<String> roles;
    private List<String> permissions;

    public LoginResponse() {}

    public LoginResponse(String token, Long userId, String username, String realName, String avatar, 
                        Long expiresIn, List<String> roles, List<String> permissions) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.avatar = avatar;
        this.expiresIn = expiresIn;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
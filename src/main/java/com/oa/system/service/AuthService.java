package com.oa.system.service;

import com.oa.system.dto.LoginRequest;
import com.oa.system.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String token);
    LoginResponse getCurrentUser(String token);
}

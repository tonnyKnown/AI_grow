package com.oa.system.interceptor;

import com.oa.system.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtBlacklistInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistInterceptor.class);
    private static final String AUTH_HEADER = "Authorization";

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(AUTH_HEADER);

        if (token != null && !token.isEmpty()) {
            if (authService.isTokenBlacklisted(token)) {
                log.warn("请求被拒绝: token已在黑名单中");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"token已失效，请重新登录\"}");
                return false;
            }
        }

        return true;
    }
}

package com.oa.gateway.filter;

import com.oa.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    // 白名单路径（前端直接访问，不需要认证）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/system/auth/login",
            "/api/system/auth/logout",
            "/api/python/**"
    );

    // 服务间调用的信任密钥（Python服务调用其他服务时使用）
    @Value("${gateway.trusted-services.python-service-key:python-service-secret-key-2024}")
    private String pythonServiceKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("请求路径: {}", path);

        // 1. 检查是否是白名单路径
        if (isWhiteListed(path)) {
            log.info("白名单路径，直接放行");
            return chain.filter(exchange);
        }

        // 2. 检查是否是信任服务发起的请求（服务间调用）
        String serviceKey = request.getHeaders().getFirst("Service-Key");
        if (serviceKey != null && serviceKey.equals(pythonServiceKey)) {
            log.info("检测到信任服务请求，跳过JWT校验");
            // 将 Service-Key 转换为 userId header，供下游服务使用
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("userId", "0")  // 服务间调用使用 userId=0 标识
                    .header("X-Username", "python-service")
                    .header("X-Service-Call", "true")
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // 3. 检查 Authorization header
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("缺少Authorization header或格式不正确");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            log.info("Token验证成功, userId: {}, username: {}", userId, username);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("userId", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            log.error("Token验证异常", e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isWhiteListed(String path) {
        for (String whitePath : WHITE_LIST) {
            if (whitePath.endsWith("/**")) {
                // 处理通配符路径
                String prefix = whitePath.substring(0, whitePath.length() - 3);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -2000000;
    }
}

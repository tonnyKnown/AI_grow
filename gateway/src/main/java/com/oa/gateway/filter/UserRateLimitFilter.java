package com.oa.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.oa.gateway.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserRateLimitFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "userId";
    private static final String RATE_LIMIT_PREFIX = "gateway:ratelimit:user:";
    private static final String TOO_MANY_REQUESTS_MESSAGE = "请求过于频繁，请稍后再试";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${gateway.ratelimit.per-user.count:5}")
    private int perUserCount;

    @Value("${gateway.ratelimit.per-user.window-seconds:1}")
    private int windowSeconds;

    public UserRateLimitFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 从请求头获取用户 ID，未登录用户标记为 anonymous
        String userId = request.getHeaders().getFirst(USER_ID_HEADER);
        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }

        // 构建限流 Key：网关:限流:用户:用户ID:请求路径（按用户+路径限流，每个接口独立计数）
        String path = request.getURI().getPath();
        String key = RATE_LIMIT_PREFIX + userId + ":" + path;

        try {
            // Redis 原子递增计数器
            Long count = redisTemplate.opsForValue().increment(key);
            
            // 首次请求时设置过期时间，实现滑动窗口
            if (count == 1) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }

            // 判断是否超过限流阈值
            if (count > perUserCount) {
                log.warn("用户维度限流触发: userId={}, path={}, count={}, limit={}/{}s", userId, path, count, perUserCount, windowSeconds);
                return handleRateLimit(exchange);
            }

            // 限流通过，继续执行后续过滤器链
            return chain.filter(exchange);
        } catch (Exception e) {
            // Redis 异常时放行，避免影响正常请求
            log.error("用户维度限流异常: userId={}, path={}", userId, path, e);
            return chain.filter(exchange);
        }
    }

    /**
     * 处理限流请求，返回 429 状态码和错误信息
     * @param exchange 网关请求交换对象
     * @return Mono<Void> 响应流
     */
    private Mono<Void> handleRateLimit(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Result<Object> result = Result.error(429, TOO_MANY_REQUESTS_MESSAGE);
        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 获取过滤器执行顺序，优先级极高确保在认证等过滤器之前执行
     * @return 过滤器顺序值
     */
    @Override
    public int getOrder() {
        return -1999999;
    }
}
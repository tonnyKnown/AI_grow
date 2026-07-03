package com.oa.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.gateway.common.Result;
import com.oa.gateway.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class GlobalExceptionFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    log.error("网关异常: ", throwable);
                    return handleException(exchange, throwable);
                });
    }

    private Mono<Void> handleException(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Result<Void> result;
        HttpStatus status;

        if (throwable instanceof ResponseStatusException rse) {
            status = (HttpStatus) rse.getStatusCode();
            String reason = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
            result = Result.error(status.value(), reason);
        } else if (throwable.getCause() instanceof java.net.ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            result = Result.error(ErrorCode.SERVICE_UNAVAILABLE.getCode(), "服务连接失败，请稍后重试");
        } else if (throwable.getCause() instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            result = Result.error(504, "请求超时，请稍后重试");
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result = Result.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
        }

        response.setStatusCode(status);

        try {
            byte[] bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化响应失败", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

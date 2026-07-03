package com.oa.gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SYSTEM_ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    GATEWAY_ERROR(502, "网关错误");

    private final Integer code;
    private final String message;
}

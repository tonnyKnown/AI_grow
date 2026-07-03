package com.example.javachain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 API 返回结果结构
 *
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码：200 成功，其他失败
     */
    private Integer code;

    /**
     * 消息描述
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 成功返回（无数据）
     */
    public static <T> ApiResult<T> success() {
        return ApiResult.<T>builder()
                .code(200)
                .success(true)
                .message("操作成功")
                .build();
    }

    /**
     * 成功返回（带消息）
     */
    public static <T> ApiResult<T> success(String message) {
        return ApiResult.<T>builder()
                .code(200)
                .success(true)
                .message(message)
                .data((T) message)
                .build();
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> ApiResult<T> success(T data) {
        return ApiResult.<T>builder()
                .code(200)
                .success(true)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 成功返回（带消息和数据）
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return ApiResult.<T>builder()
                .code(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败返回（默认）
     */
    public static <T> ApiResult<T> error() {
        return ApiResult.<T>builder()
                .code(500)
                .success(false)
                .message("操作失败")
                .build();
    }

    /**
     * 失败返回（带消息）
     */
    public static <T> ApiResult<T> error(String message) {
        return ApiResult.<T>builder()
                .code(500)
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 失败返回（带状态码和消息）
     */
    public static <T> ApiResult<T> error(Integer code, String message) {
        return ApiResult.<T>builder()
                .code(code)
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 失败返回（带数据）
     */
    public static <T> ApiResult<T> error(String message, T data) {
        return ApiResult.<T>builder()
                .code(500)
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败返回（带数据，无消息）
     */
    public static <T> ApiResult<T> error(T data) {
        return ApiResult.<T>builder()
                .code(500)
                .success(false)
                .message("操作失败")
                .data(data)
                .build();
    }

    /**
     * 失败返回（带状态码、消息和数据）
     */
    public static <T> ApiResult<T> error(Integer code, String message, T data) {
        return ApiResult.<T>builder()
                .code(code)
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 自定义返回
     */
    public static <T> ApiResult<T> of(Integer code, Boolean success, String message, T data) {
        return ApiResult.<T>builder()
                .code(code)
                .success(success)
                .message(message)
                .data(data)
                .build();
    }
}

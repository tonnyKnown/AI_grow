package com.oa.gateway.controller;

import com.oa.gateway.common.Result;
import com.oa.gateway.exception.BusinessException;
import com.oa.gateway.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/test")
public class TestController {

    @GetMapping("/success")
    public Result<String> success() {
        return Result.success("网关运行正常");
    }

    @GetMapping("/business-exception")
    public Result<String> businessException() {
        throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "测试业务异常");
    }

    @GetMapping("/system-exception")
    public Result<String> systemException() {
        throw new RuntimeException("测试系统异常");
    }

    @GetMapping("/illegal-argument")
    public Result<String> illegalArgument() {
        throw new IllegalArgumentException("测试参数异常");
    }
}

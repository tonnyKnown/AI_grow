package com.oa.business.aspect;

import java.lang.annotation.*;

/**
 * 接口调用日志注解
 * 在需要记录日志的 Controller 方法上添加此注解即可
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    
    /**
     * 接口描述（可选）
     */
    String value() default "";
    
    /**
     * 是否记录请求参数（默认是）
     */
    boolean logParams() default true;
    
    /**
     * 是否记录返回结果（默认否，避免返回大数据）
     */
    boolean logResult() default false;
}

package com.oa.business.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口调用日志切面
 */
@Aspect
@Component
public class ApiLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiLogAspect.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切点：使用 @ApiLog 注解的方法
     */
    @Pointcut("@annotation(com.oa.business.aspect.ApiLog)")
    public void apiLogPointcut() {
    }

    /**
     * 环绕通知：记录接口调用前后的日志
     */
    @Around("apiLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiLog apiLogAnnotation = method.getAnnotation(ApiLog.class);

        // 构建方法描述
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        String apiDescription = apiLogAnnotation.value().isEmpty() ? fullMethodName : apiLogAnnotation.value();

        // 记录接口调用开始
        log.info("========== 接口调用开始 ==========");
        log.info("接口名称: {}", apiDescription);
        log.info("请求URL: {}", request.getRequestURL());
        log.info("请求方法: {}", request.getMethod());
        log.info("请求IP: {}", request.getRemoteAddr());

        // 记录接口参数（如果需要）
        if (apiLogAnnotation.logParams()) {
            Map<String, Object> params = new HashMap<>();
            
            // 记录 @RequestParam 参数
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                params.put(paramName, request.getParameter(paramName));
            }

            // 记录 @PathVariable 和 @RequestBody 参数
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && !isSimpleType(args[i].getClass())) {
                    // 如果是复杂对象，转换为 JSON
                    try {
                        params.put("arg" + i, objectMapper.writeValueAsString(args[i]));
                    } catch (Exception e) {
                        params.put("arg" + i, args[i].toString());
                    }
                } else if (args[i] != null) {
                    // 简单类型
                    params.put("arg" + i, args[i].toString());
                }
            }
            log.info("接口参数: {}", params);
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录返回结果（如果需要）
            if (apiLogAnnotation.logResult() && result != null) {
                try {
                    String resultStr = objectMapper.writeValueAsString(result);
                    // 限制返回结果长度，避免日志过大
                    if (resultStr.length() > 1000) {
                        resultStr = resultStr.substring(0, 1000) + "...";
                    }
                    log.info("接口返回: {}", resultStr);
                } catch (Exception e) {
                    log.info("接口返回: {}", result.toString());
                }
            }
            
            return result;
        } catch (Throwable e) {
            log.error("接口调用异常: {}", apiDescription, e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            log.info("接口执行耗时: {}ms", costTime);
            log.info("========== 接口调用结束 ==========");
        }
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz == String.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Boolean.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Short.class ||
               clazz == Byte.class;
    }
}

package com.example.javachain.service;

import dev.langchain4j.agent.tool.Tool;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 统一的工具服务 - 所有 MCP 工具都在这里定义
 * 使用 @Tool 注解让 McpService 自动发现和注册
 * <p>
 * 优化点：
 * 1. 更完善的工具实现
 * 2. 更好的参数验证
 * 3. 更友好的错误信息
 * 4. 添加更多实用工具
 */
@Service
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Resource
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret:oa-system-jwt-secret-key-2024-for-secure-authentication-very-long-enough}")
    private String jwtSecret;

    private RagService ragService;    // ==================== 时间相关工具 ====================


    @Tool("获取当前系统时间，格式：yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        String time = LocalDateTime.now().format(DEFAULT_DATETIME_FORMATTER);
        log.debug("获取当前时间: {}", time);
        return time;
    }


    @Tool("格式化时间戳为可读格式，参数：timestamp(秒)")
    public String formatTimestamp(long timestamp) {
        try {
            String time = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    java.time.ZoneId.systemDefault()
            ).format(DEFAULT_DATETIME_FORMATTER);
            log.debug("格式化时间戳 {} -> {}", timestamp, time);
            return time;
        } catch (Exception e) {
            log.warn("时间戳格式化失败: {}", e.getMessage());
            return "错误：无效的时间戳";
        }
    }

    // ==================== 随机数相关工具 ====================

    @Tool("生成指定范围内的随机数，参数：min(最小值), max(最大值)")
    public String generateRandomNumber(int min, int max) {
        if (min > max) {
            String error = String.format("错误：最小值(%d)不能大于最大值(%d)", min, max);
            log.warn(error);
            return error;
        }

        int result = RANDOM.nextInt(max - min + 1) + min;
        String message = String.format("随机数 [%d, %d]: %d", min, max, result);
        log.debug(message);
        return message;
    }

    @Tool("生成随机 UUID")
    public String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        log.debug("生成 UUID: {}", uuid);
        return uuid;
    }

    @Tool("查询知识库信息,参数：question(问题)")
    public String queryKnowledgeBase(String question) {
        String response =   ragService.query(question);
        log.debug("生成 response: {}", response);
        return response;
    }

    @Tool("根据 JWT token 获取当前登录用户信息，参数：token，支持 Bearer 前缀或纯 token")
    public String getCurrentUserFromToken() {
        try {
           String token = resolveToken();
            if (token == null || token.isBlank()) {
                return "解析失败：token 不能为空";
            }
            Claims claims = parseClaims(token);
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("userId", claims.get("userId"));
            userInfo.put("username", claims.getSubject());
            userInfo.put("subject", claims.getSubject());
            userInfo.put("issuedAt", claims.getIssuedAt());
            userInfo.put("expiration", claims.getExpiration());
            userInfo.put("claims", new LinkedHashMap<>(claims));
            return objectMapper.writeValueAsString(userInfo);
        } catch (Exception e) {
            log.warn("Failed to parse JWT token for current user: {}", e.getMessage());
            return "解析失败：" + e.getMessage();
        }
    }

    private String resolveToken() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }



    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private Claims parseClaims(String token) {
        String cleanToken = token.trim();
        if (cleanToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            cleanToken = cleanToken.substring(7).trim();
        }
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(cleanToken)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

}

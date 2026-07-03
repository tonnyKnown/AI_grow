package com.example.javachain.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * 安全服务
 * 提供权限控制、敏感词检查、审计日志
 */
@Slf4j
@Service
public class SecurityService {

    private final ConcurrentLinkedQueue<AuditLog> auditLogs = new ConcurrentLinkedQueue<>();

    private static final List<String> DANGEROUS_TOOLS = List.of(
        "filesystem_write", "execute"
    );

    private static final List<String> SENSITIVE_PATTERNS = List.of(
        "rm -rf", "delete *", "drop table", "shutdown"
    );

    public boolean checkSensitiveContent(String content) {
        for (String pattern : SENSITIVE_PATTERNS) {
            if (content.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean requiresApproval(String toolName) {
        return DANGEROUS_TOOLS.contains(toolName);
    }


    public void logAction(String userId, String action, String toolName, 
                          String arguments, String result, boolean isApproved) {
        AuditLog auditLog = new AuditLog(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            userId,
            action,
            toolName,
            arguments,
            result,
            isApproved
        );
        auditLogs.add(auditLog);
        log.info("审计日志 - 用户: {}, 动作: {}, 工具: {}, 批准: {}", 
                 userId, action, toolName, isApproved);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        List<AuditLog> recent = new ArrayList<>();
        int count = 0;
        for (AuditLog log : auditLogs) {
            if (count++ >= limit) break;
            recent.add(log);
        }
        return recent;
    }

    public List<AuditLog> getAllLogs() {
        return new ArrayList<>(auditLogs);
    }
}

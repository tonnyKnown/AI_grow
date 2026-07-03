package com.example.javachain.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 安全审计服务
 *
 * 记录所有工具调用，用于安全审计和合规
 */
@Slf4j
@Service
public class SecurityAuditService {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 审计日志内存缓存（生产环境建议使用数据库或 Elasticsearch） */
    private final List<AuditEntry> auditLog = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_AUDIT_ENTRIES = 10000;

    public SecurityAuditService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 记录工具调用
     */
    public void logToolCall(String traceId, String toolName, Map<String, Object> arguments,
                            String userId, String sessionId, boolean success, String result) {
        if (auditLog.size() >= MAX_AUDIT_ENTRIES) {
            // 清理旧日志，保留最近 1000 条
            synchronized (auditLog) {
                while (auditLog.size() > 1000) {
                    auditLog.remove(0);
                }
            }
        }

        AuditEntry entry = new AuditEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setTimestamp(LocalDateTime.now().format(formatter));
        entry.setType("TOOL_CALL");
        entry.setTraceId(traceId);
        entry.setToolName(toolName);
        entry.setArguments(sanitizeArguments(arguments));
        entry.setUserId(userId);
        entry.setSessionId(sessionId);
        entry.setSuccess(success);
        entry.setResult(truncateResult(result));
        entry.setDuration(System.currentTimeMillis());

        auditLog.add(entry);

        // 打印审计日志
        if (!success) {
            log.warn("🔒 [AUDIT] 工具调用失败 - traceId={}, tool={}, user={}, reason={}",
                    traceId, toolName, userId, result);
        } else {
            log.info("🔒 [AUDIT] 工具调用 - traceId={}, tool={}, user={}",
                    traceId, toolName, userId);
        }
    }

    /**
     * 记录敏感操作
     */
    public void logSensitiveOperation(String traceId, String operation, Map<String, Object> details,
                                      String userId, boolean approved) {
        AuditEntry entry = new AuditEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setTimestamp(LocalDateTime.now().format(formatter));
        entry.setType("SENSITIVE_OPERATION");
        entry.setTraceId(traceId);
        entry.setToolName(operation);
        entry.setArguments(details);
        entry.setUserId(userId);
        entry.setSuccess(approved);
        entry.setResult(approved ? "APPROVED" : "REJECTED");

        auditLog.add(entry);

        if (!approved) {
            log.warn("🔒 [AUDIT] 敏感操作被拒绝 - traceId={}, operation={}, user={}",
                    traceId, operation, userId);
        }
    }

    /**
     * 记录认证事件
     */
    public void logAuthEvent(String userId, String event, boolean success, String details) {
        AuditEntry entry = new AuditEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setTimestamp(LocalDateTime.now().format(formatter));
        entry.setType("AUTH");
        entry.setUserId(userId);
        entry.setSuccess(success);
        entry.setResult(event + ": " + details);

        auditLog.add(entry);

        if (!success) {
            log.warn("🔒 [AUDIT] 认证失败 - user={}, event={}, details={}",
                    userId, event, details);
        }
    }

    /**
     * 获取审计日志
     */
    public List<AuditEntry> getAuditLogs(int limit, int offset) {
        synchronized (auditLog) {
            int size = auditLog.size();
            int fromIndex = Math.max(0, size - offset - limit);
            int toIndex = Math.max(0, size - offset);
            return new ArrayList<>(auditLog.subList(fromIndex, toIndex));
        }
    }

    /**
     * 按用户查询审计日志
     */
    public List<AuditEntry> getAuditLogsByUser(String userId, int limit) {
        return auditLog.stream()
                .filter(e -> userId.equals(e.getUserId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 按工具名称查询审计日志
     */
    public List<AuditEntry> getAuditLogsByTool(String toolName, int limit) {
        return auditLog.stream()
                .filter(e -> toolName.equals(e.getToolName()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取审计统计
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = auditLog.size();
        long success = auditLog.stream().filter(AuditEntry::isSuccess).count();
        long failed = total - success;

        stats.put("total", total);
        stats.put("success", success);
        stats.put("failed", failed);
        stats.put("successRate", total > 0 ? String.format("%.2f%%", (success * 100.0 / total)) : "N/A");

        // 按工具统计
        Map<String, Long> toolStats = auditLog.stream()
                .collect(Collectors.groupingBy(AuditEntry::getToolName, Collectors.counting()));
        stats.put("toolCounts", toolStats);

        // 按类型统计
        Map<String, Long> typeStats = auditLog.stream()
                .collect(Collectors.groupingBy(AuditEntry::getType, Collectors.counting()));
        stats.put("typeCounts", typeStats);

        return stats;
    }

    /**
     * 清理过期审计日志
     */
    public void cleanOldLogs(int maxAgeHours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(maxAgeHours);
        synchronized (auditLog) {
            auditLog.removeIf(entry -> {
                try {
                    LocalDateTime entryTime = LocalDateTime.parse(entry.getTimestamp(), formatter);
                    return entryTime.isBefore(cutoff);
                } catch (Exception e) {
                    return true;
                }
            });
        }
        log.info("清理审计日志完成，当前保留 {} 条", auditLog.size());
    }

    /**
     * 脱敏参数（隐藏敏感字段）
     */
    private Map<String, Object> sanitizeArguments(Map<String, Object> arguments) {
        if (arguments == null) return Map.of();

        Set<String> sensitiveKeys = Set.of(
                "password", "passwd", "secret", "token", "apiKey", "api_key",
                "accessKey", "access_key", "privateKey", "private_key",
                "credential", "auth"
        );

        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (sensitiveKeys.stream().anyMatch(key::contains)) {
                sanitized.put(entry.getKey(), "***REDACTED***");
            } else if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                // 对可能包含敏感信息的长字符串进行截断
                if (value.length() > 100) {
                    sanitized.put(entry.getKey(), value.substring(0, 50) + "...(truncated)");
                } else {
                    sanitized.put(entry.getKey(), value);
                }
            } else {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    /**
     * 截断结果（防止日志过长）
     */
    private String truncateResult(String result) {
        if (result == null) return null;
        if (result.length() > 500) {
            return result.substring(0, 200) + "...(truncated)";
        }
        return result;
    }

    /**
     * 审计条目
     */
    @lombok.Data
    public static class AuditEntry {
        private String id;
        private String timestamp;
        private String type;
        private String traceId;
        private String toolName;
        private Map<String, Object> arguments;
        private String userId;
        private String sessionId;
        private boolean success;
        private String result;
        private long duration;
    }
}

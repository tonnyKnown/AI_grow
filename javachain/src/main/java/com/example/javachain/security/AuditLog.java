package com.example.javachain.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private String id;
    private LocalDateTime timestamp;
    private String userId;
    private String action;
    private String toolName;
    private String arguments;
    private String result;
    private boolean isApproved;
}

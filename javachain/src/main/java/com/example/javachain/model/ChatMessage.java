package com.example.javachain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    /**
     * 角色：user 或 assistant
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
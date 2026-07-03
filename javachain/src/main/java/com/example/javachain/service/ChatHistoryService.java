package com.example.javachain.service;

import com.example.javachain.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话历史缓存服务
 * 使用本地内存缓存，支持多会话管理
 */
@Service
public class ChatHistoryService {

    /**
     * 存储所有会话的历史记录
     * Key: sessionId
     * Value: 消息列表
     */
    private final Map<String, List<ChatMessage>> chatHistory = new ConcurrentHashMap<>();

    /**
     * 最大历史消息数（超过后自动清理）
     */
    private static final int MAX_HISTORY_SIZE = 50;

    /**
     * 创建新会话
     *
     * @return 会话ID
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        chatHistory.put(sessionId, new ArrayList<>());
        return sessionId;
    }

    /**
     * 获取会话历史
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<ChatMessage> getHistory(String sessionId) {
        return chatHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    /**
     * 添加消息到历史
     *
     * @param sessionId 会话ID
     * @param role      角色
     * @param content   内容
     */
    public void addMessage(String sessionId, String role, String content) {
        List<ChatMessage> history = chatHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        ChatMessage message = ChatMessage.builder()
                .role(role)
                .content(content)
                .build();
        
        history.add(message);
        
        // 保持历史记录在限制范围内
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    /**
     * 清空会话历史
     *
     * @param sessionId 会话ID
     */
    public void clearHistory(String sessionId) {
        List<ChatMessage> history = chatHistory.get(sessionId);
        if (history != null) {
            history.clear();
        }
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    public void deleteSession(String sessionId) {
        chatHistory.remove(sessionId);
    }

    /**
     * 获取所有会话ID
     *
     * @return 会话ID列表
     */
    public List<String> getAllSessions() {
        return new ArrayList<>(chatHistory.keySet());
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean sessionExists(String sessionId) {
        return chatHistory.containsKey(sessionId);
    }
}
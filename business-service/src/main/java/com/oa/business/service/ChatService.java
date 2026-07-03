package com.oa.business.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private OpenAIService openAIService;
    
    public String processMessageOrders(String message) {
        log.info("处理消息: {}", message);
        
        // 调用OpenAI服务（如果配置了API Key，会调用真实API，否则使用本地智能回复）
        return openAIService.chatOrders(message, null);
    }

    public String processMessageRobot(String message) {
        log.info("处理消息: {}", message);

        // 调用OpenAI服务（如果配置了API Key，会调用真实API，否则使用本地智能回复）
        return openAIService.chatRobot(message, null);
    }

    public Map<String, Object> processMessageKnowledge(String message, String sessionId, String userId) {
        log.info("处理知识百科消息: {}, 会话ID: {}, 用户ID: {}", message, sessionId, userId);

        // 调用OpenAI服务（Python后端）
        return openAIService.chatKnowledge(message, sessionId, userId, null);

    }

    public Map<String, Object> getKnowledgeHistory(String sessionId) {
        log.info("获取知识百科聊天历史: {}", sessionId);
        return openAIService.getChatHistory(sessionId);
    }

    public boolean clearKnowledgeSession(String sessionId) {
        log.info("清空知识百科会话: {}", sessionId);
        return openAIService.clearSession(sessionId);
    }
}
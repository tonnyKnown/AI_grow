package com.example.javachain.service;

import com.example.javachain.model.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ChatService {

    private static final String AI_SERVICE_CIRCUIT_BREAKER = "aiService";

    private final ChatLanguageModel chatLanguageModel;
    private final ChatHistoryService chatHistoryService;

    public ChatService(ChatLanguageModel chatLanguageModel, ChatHistoryService chatHistoryService) {
        this.chatLanguageModel = chatLanguageModel;
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 简单对话（不带历史）
     */
    @CircuitBreaker(name = AI_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "chatFallback")
    public String chat(String message) {
        log.debug("Calling AI chat service with message length: {}", message != null ? message.length() : 0);
        return chatLanguageModel.generate(message);
    }

    public String chatFallback(String message, Throwable throwable) {
        log.warn("AI service fallback triggered: {}", throwable.getMessage());
        return "抱歉，AI服务暂时不可用，请稍后再试。";
    }

    /**
     * 带历史的对话
     *
     * @param sessionId 会话ID
     * @param message   用户消息
     * @return 回复
     */
    @CircuitBreaker(name = AI_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "chatWithHistoryFallback")
    public String chatWithHistory(String sessionId, String message) {
        log.debug("Calling AI chat with history, sessionId: {}, message length: {}", 
                sessionId, message != null ? message.length() : 0);
        // 添加用户消息到历史
        chatHistoryService.addMessage(sessionId, "user", message);
        
        // 构建对话历史
        List<ChatMessage> history = chatHistoryService.getHistory(sessionId);
        
        // 构建带历史的提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个友好的助手，帮助用户解答问题。\n\n");
        prompt.append("对话历史：\n");
        
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                prompt.append("用户：").append(msg.getContent()).append("\n");
            } else {
                prompt.append("助手：").append(msg.getContent()).append("\n");
            }
        }
        
        // 添加当前问题
        prompt.append("\n用户：").append(message).append("\n");
        prompt.append("助手：");
        
        // 生成回复
        String response = chatLanguageModel.generate(prompt.toString());
        
        // 添加助手回复到历史
        chatHistoryService.addMessage(sessionId, "assistant", response);
        
        return response;
    }

    public String chatWithHistoryFallback(String sessionId, String message, Throwable throwable) {
        log.warn("AI service fallback triggered for session {}: {}", sessionId, throwable.getMessage());
        String fallbackResponse = "抱歉，AI服务暂时不可用，请稍后再试。";
        chatHistoryService.addMessage(sessionId, "assistant", fallbackResponse);
        return fallbackResponse;
    }

}
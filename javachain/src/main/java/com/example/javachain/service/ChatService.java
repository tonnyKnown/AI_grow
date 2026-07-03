package com.example.javachain.service;

import com.example.javachain.model.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final ChatHistoryService chatHistoryService;

    public ChatService(ChatLanguageModel chatLanguageModel, ChatHistoryService chatHistoryService) {
        this.chatLanguageModel = chatLanguageModel;
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 简单对话（不带历史）
     */
    public String chat(String message) {
        return chatLanguageModel.generate(message);
    }

    /**
     * 带历史的对话
     *
     * @param sessionId 会话ID
     * @param message   用户消息
     * @return 回复
     */
    public String chatWithHistory(String sessionId, String message) {
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

    /**
     * 带上下文的对话（兼容旧接口）
     */
    public String chatWithContext(List<String> messages) {
        StringBuilder conversation = new StringBuilder();
        for (String msg : messages) {
            conversation.append(msg).append("\n");
        }
        return chatLanguageModel.generate(conversation.toString());
    }
}
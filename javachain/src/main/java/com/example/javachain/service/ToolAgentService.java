package com.example.javachain.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Service;

/**
 * 智能工具选择服务
 * 使用 LangChain4j AiServices 自动选择并执行工具
 */
@Service
public class ToolAgentService {

    private final IntelligentAgent agent;

    public ToolAgentService(ChatLanguageModel chatLanguageModel, SkillService skillService) {
        this.agent = AiServices.builder(IntelligentAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .tools(skillService)
            .build();
    }

    public String executeWithAutoTool(String question) {
        return agent.chat("default", question);
    }

    interface IntelligentAgent {
        @SystemMessage("你是一个智能助手，可以使用工具来帮助用户。根据用户问题选择最合适的工具，如果不需要工具则直接回答。简要回答。")
        String chat(@MemoryId String memoryId, @UserMessage String userMessage);
    }
}

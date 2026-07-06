package com.example.javachain.controller;

import com.example.javachain.common.ApiResult;
import com.example.javachain.model.ChatMessage;
import com.example.javachain.service.ChatHistoryService;
import com.example.javachain.service.ChatService;
import com.example.javachain.service.FileVectorService;
import com.example.javachain.service.IntelligentChatService;
import com.example.javachain.service.McpService;
import com.example.javachain.service.RagService;
import com.example.javachain.service.SkillService;
import com.example.javachain.service.ToolAgentService;
import com.example.javachain.service.WeatherAgentClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天控制器 - 统一的 API 入口
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatHistoryService chatHistoryService;
    private final RagService ragService;
    private final SkillService skillService;
    private final IntelligentChatService intelligentChatService;
    private final McpService mcpService;
    private final FileVectorService fileVectorService;
    private final ToolAgentService toolAgentService;
    private final WeatherAgentClient weatherAgentClient;

    public ChatController(ChatService chatService, ChatHistoryService chatHistoryService,
                         RagService ragService, SkillService skillService,
                         IntelligentChatService intelligentChatService,
                         McpService mcpService, FileVectorService fileVectorService,
                         ToolAgentService toolAgentService,
                         WeatherAgentClient weatherAgentClient) {
        this.chatService = chatService;
        this.chatHistoryService = chatHistoryService;
        this.ragService = ragService;
        this.skillService = skillService;
        this.intelligentChatService = intelligentChatService;
        this.mcpService = mcpService;
        this.fileVectorService = fileVectorService;
        this.toolAgentService = toolAgentService;
        this.weatherAgentClient = weatherAgentClient;
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session/create")
    public ApiResult<Map<String, String>> createSession() {
        String sessionId = chatHistoryService.createSession();
        return ApiResult.success(Map.of("sessionId", sessionId));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ApiResult<String> deleteSession(@PathVariable String sessionId) {
        chatHistoryService.deleteSession(sessionId);
        return ApiResult.success("会话已删除");
    }

    /**
     * 清空会话历史
     */
    @PostMapping("/session/{sessionId}/clear")
    public ApiResult<String> clearSession(@PathVariable String sessionId) {
        chatHistoryService.clearHistory(sessionId);
        return ApiResult.success("会话历史已清空");
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/session/{sessionId}/history")
    public ApiResult<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = chatHistoryService.getHistory(sessionId);
        return ApiResult.success(history);
    }

    /**
     * 简单对话（不带历史）
     */
    @PostMapping("/simple")
    public ApiResult<String> simpleChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = chatService.chat(message);
        return ApiResult.success(response);
    }

    /**
     * 带历史的对话
     */
    @PostMapping("/simple/with-history")
    public ApiResult<String> simpleChatWithHistory(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message");
        
        // 如果没有会话ID，创建新会话
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = chatHistoryService.createSession();
        }
        
        String response = intelligentChatService.chatWithHistory(sessionId, message);
        return ApiResult.success(response);
    }

    @PostMapping("/auto")
    public ApiResult<String> autoChat(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String response = toolAgentService.executeWithAutoTool(question);
        return ApiResult.success(response);
    }

    @PostMapping("/rag")
    public ApiResult<String> ragChat(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String response = ragService.query(question);
        return ApiResult.success(response);
    }

    @PostMapping("/rag/load")
    public ApiResult<String> loadDocument(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String title = request.getOrDefault("title", "Untitled");
        ragService.loadTextAsDocument(text, title);
        return ApiResult.success("文档加载成功");
    }

    @GetMapping("/rag/stats")
    public ApiResult<String> getRagStats() {
        return ApiResult.success(fileVectorService.getKnowledgeBaseStats());
    }

    @PostMapping("/rag/clear")
    public ApiResult<String> clearKnowledgeBase() {
        ragService.clearKnowledgeBase();
        return ApiResult.success("知识库已清空");
    }

    @GetMapping("/files/list")
    public ApiResult<List<String>> listFiles(@RequestParam(defaultValue = "") String directory) {
        List<String> files = fileVectorService.listFiles(directory);
        return ApiResult.success(files);
    }

    @GetMapping("/files/vectorize")
    public ApiResult<String> vectorizeFiles() {
        String result = fileVectorService.loadFilesFromDirectory("D:\\agentDemo\\javachain\\src\\main\\resources\\file\\");
        return ApiResult.success(result);
    }

    @PostMapping("/files/vectorize/single")
    public ApiResult<String> vectorizeSingleFile(@RequestBody Map<String, String> request) {
        String filePath = request.get("filePath");
        String result = fileVectorService.loadSingleFile(filePath);
        return ApiResult.success(result);
    }

    @PostMapping("/mcp/execute")
    public ApiResult<String> executeMcp(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("toolName");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
        String response = mcpService.executeTool(toolName, arguments);
        return ApiResult.success(response);
    }

    @GetMapping("/mcp/tools")
    public ApiResult<String> getMcpTools() {
        return ApiResult.success(mcpService.getAvailableTools());
    }

    /**
     * 调用天气 Agent（通过 Nacos A2A 协议）
     */
    @GetMapping("/weather")
    public ApiResult<String> queryWeather(@RequestParam String city) {
        if (city == null || city.isBlank()) {
            return ApiResult.error("请提供 city 参数");
        }

        if (!weatherAgentClient.isAgentAvailable()) {
            return ApiResult.error("天气 Agent 不在线，请检查 ai-weather-agent 是否已注册到 Nacos");
        }

        String response = weatherAgentClient.queryWeather(city);
        return ApiResult.success(response);
    }
}

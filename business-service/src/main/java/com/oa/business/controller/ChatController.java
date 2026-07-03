package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.service.ChatService;
import feign.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/business/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到sendMessage聊天消息: {}", message);
        
        String reply = chatService.processMessageRobot(message);
        
        return Result.success(Map.of(
            "reply", reply,
            "sessionId", "session-" + System.currentTimeMillis()
        ));
    }
    @PostMapping("/chatRobot")
    public Result<Map<String, Object>> chatRobot(@RequestBody Map<String, Object> request,
                                                 @RequestHeader(value = "userId") Long userId) {
        String message = (String) request.get("message");
        String sessionId = (String) request.getOrDefault("session_id", "session-") + userId;
        log.info("收到chatRobot聊天消息: {}, 会话ID: {}, 用户ID: {}", message, sessionId, userId);

        Map<String, Object> result = chatService.processMessageKnowledge(message, sessionId, String.valueOf(userId));
        return Result.success(result);
    }

    @GetMapping("/history/{sessionId}")
    public Result<Object> getHistory(@PathVariable String sessionId,
                                     @RequestHeader(value = "userId") Long userId) {
        log.info("查询聊天历史: {}", sessionId);
        Map<String, Object> history = chatService.getKnowledgeHistory(sessionId+userId);
        if (history != null) {
            return Result.success(history);
        } else {
            return Result.error("获取历史记录失败");
        }
    }

    @PostMapping("/session/clear")
    public Result<Object> clearSession(@RequestBody Map<String, String> request) {
        String sessionId = request.get("session_id");
        log.info("清空会话: {}", sessionId);
        boolean success = chatService.clearKnowledgeSession(sessionId);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error("清空会话失败");
        }
    }

    @PostMapping("/session")
    public Result<Map<String, String>> createSession() {
        String sessionId = "session-" + System.currentTimeMillis();
        log.info("创建新会话: {}", sessionId);
        return Result.success(Map.of("sessionId", sessionId));
    }
}
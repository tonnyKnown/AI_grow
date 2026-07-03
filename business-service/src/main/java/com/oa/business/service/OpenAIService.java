package com.oa.business.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.business.config.OpenAIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    @Autowired
    private OpenAIConfig openAIConfig;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    }

    public String chatOrders(String message,List<Map<String, String>> history) {

        return chat(message, history,"/agent/orders");
    }
    public String chatRobot(String message,List<Map<String, String>> history) {
        return chat(message, history,"/chat/completions");
    }

    public Map<String, Object> chatKnowledge(String message, String sessionId, String userId, List<Map<String, String>> history) {
        log.info("开始处理知识百科消息: {}", message);

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("session_id", sessionId);
            requestBody.put("user_id", userId);
            if (history != null && !history.isEmpty()) {
                requestBody.put("history", history);
            }

            log.info("请求URL: http://localhost:8000/business/chat/chatRobot");
            log.info("请求体: {}", objectMapper.writeValueAsString(requestBody));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            // 构建HttpEntity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("请求已构建，准备发送...");

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange("http://localhost:8000/business/chat/chatRobot",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应体内容: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.get("data");

                Map<String, Object> result = new HashMap<>();
                result.put("reply", data.get("reply").asText());
                result.put("sessionId", data.get("session_id").asText());

                log.info("知识百科回复: {}", result);
                return result;
            } else {
                log.error("知识百科API错误, 状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("reply", "抱歉，服务暂时不可用，请稍后重试。");
                errorResult.put("sessionId", sessionId);
                return errorResult;
            }

        } catch (Exception e) {
            log.error("知识百科API调用失败: {}", e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("reply", "抱歉，服务暂时不可用，请稍后重试。");
            errorResult.put("sessionId", sessionId);
            return errorResult;
        }
    }

    public Map<String, Object> getChatHistory(String sessionId) {
        log.info("获取聊天历史: {}", sessionId);

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            // 构建HttpEntity
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8000/chat/history/" + sessionId,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应体内容: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.get("data");

                Map<String, Object> result = new HashMap<>();
                result.put("sessionId", data.get("session_id").asText());
                result.put("messages", objectMapper.treeToValue(data.get("messages"), List.class));

                log.info("聊天历史: {}", result);
                return result;
            } else {
                log.error("获取聊天历史错误, 状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                return null;
            }

        } catch (Exception e) {
            log.error("获取聊天历史失败: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean clearSession(String sessionId) {
        log.info("清空会话: {}", sessionId);

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("session_id", sessionId);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            // 构建HttpEntity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8000/chat/session/clear",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应体内容: {}", response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("清空会话失败: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String chat(String message, List<Map<String, String>> history,String fullUrl) {
        log.info("开始处理消息: {}", message);

        try {
            String apiKey = openAIConfig.getApiKey();
            String apiUrl = openAIConfig.getApiUrl();

            log.info("API配置 - Key: {}, URL: {}, Model: {}",
                    apiKey != null ? "***" : "null", apiUrl, openAIConfig.getModel());

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getModel());

            var messages = new java.util.ArrayList<Map<String, String>>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个友好的智能客服助手，帮助用户解决问题。请用中文回答，保持简洁友好。");
            messages.add(systemMessage);

            if (history != null && !history.isEmpty()) {
                messages.addAll(history);
            }

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            log.info("请求URL: {}", fullUrl);
            log.info("请求体: {}", objectMapper.writeValueAsString(requestBody));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "application/json");

            // 构建HttpEntity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("请求已构建，准备发送...");

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange("http://localhost:8000/v1"+fullUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应体长度: {} 字符", response.getBody() != null ? response.getBody().length() : 0);
            log.info("响应体内容: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());

                if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                    String reply = root.get("choices").get(0).get("message").get("content").asText();
                    log.info("LLM回复: {}", reply);
                    return reply;
                }

                return response.getBody();
            } else {
                log.error("LLM API错误, 状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                return "抱歉，服务暂时不可用，请稍后重试。";
            }

        } catch (Exception e) {
            log.error("LLM API调用失败: {}", e.getMessage());
            e.printStackTrace();
            return "抱歉，服务暂时不可用，请稍后重试。";
        }
    }
}
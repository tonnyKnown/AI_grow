package com.example.javachain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * A2A 协议客户端 —— 通过 Nacos 发现 ai-weather-agent，调用 A2A 端点。
 */
@Slf4j
@Service
public class WeatherAgentClient {

    private final DiscoveryClient discoveryClient;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String AGENT_SERVICE_NAME = "ai-weather-agent";
    private static final String WEATHER_PATH = "/api/weather";

    public WeatherAgentClient(DiscoveryClient discoveryClient,
                              WebClient.Builder webClientBuilder,
                              ObjectMapper objectMapper) {
        this.discoveryClient = discoveryClient;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String queryWeather(String city) {
        String agentUrl = resolveAgentUrl();
        log.info("调用天气 Agent: {} -> city={}", agentUrl, city);

        try {
            String responseJson = webClient.post()
                .uri(agentUrl + WEATHER_PATH)
                .bodyValue(Map.of("city", city))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(responseJson);
            if (root.has("error")) {
                return "查询失败: " + root.get("error").asText();
            }
            return root.path("answer").asText(responseJson);

        } catch (Exception e) {
            log.error("调用天气 Agent 失败: {}", e.getMessage(), e);
            return "抱歉，暂时无法获取天气信息: " + e.getMessage();
        }
    }

    private String resolveAgentUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances(AGENT_SERVICE_NAME);
        if (instances.isEmpty())
            throw new IllegalStateException("未在 Nacos 中发现服务: " + AGENT_SERVICE_NAME);
        ServiceInstance instance = instances.get(0);
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }

    public boolean isAgentAvailable() {
        try {
            return !discoveryClient.getInstances(AGENT_SERVICE_NAME).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

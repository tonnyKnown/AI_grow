package com.example.javachain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * MCP 客户端配置
 * 
 * 配置 MCP Client 自动发现和调用远程 MCP 服务
 */
@Configuration
public class McpClientConfig {

    @Value("${nacos.server-addr:127.0.0.1:8848}")
    private String nacosServerAddr;

    @Value("${nacos.namespace:public}")
    private String namespace;

    /**
     * 创建 WebClient 用于调用 Nacos API
     */
    @Bean
    public WebClient nacosApiWebClient() {
        return WebClient.builder()
                .baseUrl("http://" + nacosServerAddr)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * MCP 服务发现器
     * 
     * 从 Nacos MCP Registry 获取已注册的 MCP 服务列表
     */
    @Bean
    public McpServiceDiscoverer mcpServiceDiscoverer(WebClient nacosApiWebClient) {
        return new McpServiceDiscoverer(nacosApiWebClient, namespace);
    }

    /**
     * MCP 服务发现器 - 从 Nacos 获取 MCP 服务列表
     */
    public static class McpServiceDiscoverer {

        private final WebClient webClient;
        private final String namespace;

        public McpServiceDiscoverer(WebClient webClient, String namespace) {
            this.webClient = webClient;
            this.namespace = namespace;
        }

        /**
         * 发现所有已注册的 MCP 服务
         * 
         * @return MCP 服务信息列表
         */
        public List<McpServerEndpoint> discoverMcpServers() {
            try {
                // 调用 Nacos AI MCP Registry API
                String response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/nacos/v3/admin/ai/mcp/list")
                                .queryParam("namespaceId", namespace)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                // TODO: 解析响应并返回 MCP 服务端点列表
                // 实际实现需要根据 Nacos API 返回格式解析
                return List.of();
            } catch (Exception e) {
                System.err.println("发现 MCP 服务失败: " + e.getMessage());
                return List.of();
            }
        }

        /**
         * 发现指定 MCP 服务的 SSE 端点
         * 
         * @param serviceName MCP 服务名称
         * @return SSE 端点 URL
         */
        public String discoverSseEndpoint(String serviceName) {
            try {
                String response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/nacos/v3/admin/ai/mcp/detail")
                                .queryParam("namespaceId", namespace)
                                .queryParam("serviceName", serviceName)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                // TODO: 解析响应获取 SSE 端点
                // 实际实现需要根据 Nacos API 返回格式解析
                return null;
            } catch (Exception e) {
                System.err.println("发现 MCP 服务端点失败: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * MCP 服务端点信息
     */
    public record McpServerEndpoint(
            String serviceName,
            String version,
            String sseEndpoint,
            List<String> tools
    ) {}
}

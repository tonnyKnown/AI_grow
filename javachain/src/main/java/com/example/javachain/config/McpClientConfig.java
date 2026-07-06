package com.example.javachain.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class McpClientConfig {

    @Value("${spring.cloud.nacos.discovery.server-addr:${nacos.server-addr:127.0.0.1:8848}}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.discovery.namespace:${nacos.namespace:public}}")
    private String namespace;

    @Value("${spring.cloud.nacos.discovery.username:${nacos.username:nacos}}")
    private String username;

    @Value("${spring.cloud.nacos.discovery.password:${nacos.password:}}")
    private String password;

    @Bean
    public WebClient nacosApiWebClient() {
        return WebClient.builder()
                .baseUrl("http://" + nacosServerAddr)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public McpServiceDiscoverer mcpServiceDiscoverer(WebClient nacosApiWebClient, ObjectMapper objectMapper) {
        return new McpServiceDiscoverer(nacosApiWebClient, objectMapper, namespace, username, password);
    }

    public static class McpServiceDiscoverer {

        private final WebClient webClient;
        private final ObjectMapper objectMapper;
        private final String namespace;
        private final String username;
        private final String password;

        public McpServiceDiscoverer(WebClient webClient, ObjectMapper objectMapper,
                                    String namespace, String username, String password) {
            this.webClient = webClient;
            this.objectMapper = objectMapper;
            this.namespace = namespace;
            this.username = username;
            this.password = password;
        }

        public List<McpServerEndpoint> discoverMcpServers() {
            try {
                String response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/nacos/v3/admin/ai/mcp/list")
                                .queryParam("namespaceId", namespace)
                                .queryParam("username", username)
                                .queryParam("password", password)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(response);
                if (root.path("code").asInt(-1) != 0) {
                    log.warn("Nacos MCP registry returned non-success response: {}", response);
                    return List.of();
                }

                JsonNode items = root.path("data").path("pageItems");
                if (!items.isArray()) {
                    return List.of();
                }

                List<McpServerEndpoint> endpoints = new ArrayList<>();
                for (JsonNode item : items) {
                    endpoints.add(new McpServerEndpoint(
                            item.path("name").asText(),
                            item.path("version").asText(),
                            item.path("protocol").asText(),
                            item.path("description").asText(),
                            item.path("enabled").asBoolean(false),
                            item.path("status").asText()
                    ));
                }
                return endpoints;
            } catch (Exception e) {
                log.warn("Failed to discover MCP services from Nacos registry: {}", e.getMessage());
                return List.of();
            }
        }

        public boolean isRegistered(String serviceName) {
            return discoverMcpServers().stream()
                    .anyMatch(server -> serviceName.equals(server.serviceName())
                            && server.enabled()
                            && "active".equalsIgnoreCase(server.status()));
        }
    }

    public record McpServerEndpoint(
            String serviceName,
            String version,
            String protocol,
            String description,
            boolean enabled,
            String status
    ) {
    }
}

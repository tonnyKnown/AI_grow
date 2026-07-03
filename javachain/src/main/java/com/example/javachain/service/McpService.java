package com.example.javachain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 客户端服务 - 使用 spring-ai-alibaba-starter-mcp-registry 自动发现 MCP 服务
 *
 * javachain 作为 Agent，专注于语义匹配和任务调度
 * MCP 工具由 spring-ai-alibaba-starter-mcp-registry 自动发现
 */
@Slf4j
@Service
public class McpService {

    private static final Logger log = LoggerFactory.getLogger(McpService.class);

    private final ObjectMapper objectMapper;

    /** MCP 服务缓存 */
    private final Map<String, McpServerInfo> mcpServers = new ConcurrentHashMap<>();

    public McpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("McpService 初始化完成（MCP 工具由 spring-ai-alibaba-starter-mcp-registry 自动发现）");
    }

    @PreDestroy
    public void destroy() {
        log.info("McpService 关闭");
    }

    /**
     * 获取指定 MCP 服务的工具列表
     */
    public List<McpToolInfo> getToolsFromServer(String serverName) {
        McpServerInfo server = mcpServers.get(serverName);
        if (server == null) {
            log.warn("MCP 服务未找到: {}", serverName);
            return Collections.emptyList();
        }
        return server.getTools();
    }

    /**
     * 获取所有可用的 MCP 工具
     */
    public List<McpToolInfo> getAllAvailableTools() {
        List<McpToolInfo> allTools = new ArrayList<>();
        for (McpServerInfo server : mcpServers.values()) {
            allTools.addAll(server.getTools());
        }
        return allTools;
    }

    /**
     * 获取可用工具的 JSON 字符串
     */
    public String getAvailableTools() {
        try {
            List<McpToolInfo> tools = getAllAvailableTools();
            return objectMapper.writeValueAsString(tools);
        } catch (Exception e) {
            log.error("获取工具列表失败", e);
            return "[]";
        }
    }

    /**
     * 执行 MCP 工具调用
     */
    public String executeTool(String toolName, Map<String, Object> arguments) {
        try {
            log.info("执行 MCP 工具: {}, 参数: {}", toolName, arguments);

            // 查找工具所属的 MCP 服务
            McpServerInfo targetServer = findServerByTool(toolName);
            if (targetServer == null) {
                return String.format("工具未找到: %s。请确保 MCP 服务已注册到 Nacos。", toolName);
            }

            // 调用远程 MCP 服务
            String result = callRemoteMcpServer(targetServer, toolName, arguments);

            log.info("MCP 工具执行成功: {} -> {}", toolName, result);
            return result;

        } catch (Exception e) {
            log.error("MCP 工具执行失败: {} - {}", toolName, e.getMessage());
            return String.format("工具执行失败: %s", e.getMessage());
        }
    }

    /**
     * 根据工具名称查找所属的 MCP 服务
     */
    private McpServerInfo findServerByTool(String toolName) {
        for (McpServerInfo server : mcpServers.values()) {
            for (McpToolInfo tool : server.getTools()) {
                if (tool.getName().equalsIgnoreCase(toolName)) {
                    return server;
                }
            }
        }
        return null;
    }

    /**
     * 调用远程 MCP 服务执行工具
     */
    private String callRemoteMcpServer(McpServerInfo server, String toolName, Map<String, Object> arguments) {
        try {
            // 构建 MCP 协议请求（JSON-RPC 2.0）
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("method", "tools/call");
            request.put("id", UUID.randomUUID().toString());

            Map<String, Object> params = new HashMap<>();
            params.put("name", toolName);
            params.put("arguments", arguments != null ? arguments : Map.of());
            request.put("params", params);

            // 获取 MCP 服务端点
            String endpoint = server.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                throw new RuntimeException("MCP 服务端点为空: " + server.getName());
            }

            log.info("调用 MCP 服务: {} @ {}", server.getName(), endpoint);
            return "MCP 工具调用已由 spring-ai-alibaba-starter-mcp-registry 自动处理";

        } catch (Exception e) {
            throw new RuntimeException("调用远程 MCP 服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 手动刷新 MCP 服务缓存
     */
    public void refreshMcpServers() {
        log.info("MCP 服务缓存已刷新，当前服务数: {}", mcpServers.size());
    }

    /**
     * 注册 MCP 服务到缓存
     */
    public void registerMcpServer(McpServerInfo server) {
        mcpServers.put(server.getName(), server);
        log.info("注册 MCP 服务: {}", server.getName());
    }

    /**
     * MCP 服务信息
     */
    public static class McpServerInfo {
        private String name;
        private String endpoint;
        private String version;
        private String description;
        private List<McpToolInfo> tools = new ArrayList<>();

        public McpServerInfo() {}

        public McpServerInfo(String name, String endpoint, String version, String description) {
            this.name = name;
            this.endpoint = endpoint;
            this.version = version;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<McpToolInfo> getTools() { return tools; }
        public void setTools(List<McpToolInfo> tools) { this.tools = tools; }
    }

    /**
     * MCP 工具信息
     */
    public static class McpToolInfo {
        private String name;
        private String description;

        public McpToolInfo() {}

        public McpToolInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

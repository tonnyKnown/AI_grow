package com.example.javachain.controller;

import com.example.javachain.common.ApiResult;
import com.example.javachain.plugin.PluginMetadata;
import com.example.javachain.service.McpService;
import com.example.javachain.service.PluginGovernanceService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 控制器。
 *
 * <p>提供 MCP Server 列表 / 注销、插件包登记以及指定工具的直连执行能力。
 * 网关侧路径为 {@code /api/javachain/mcp/**}（经 RewritePath 映射到 {@code /api/mcp/**}）。
 *
 * <p>注意：基础的「列出全部工具」「执行工具」能力已由 {@code ChatController} 的
 * {@code /api/chat/mcp/tools}、{@code /api/chat/mcp/execute} 提供，前端工具面板同时
 * 依赖这两个地址；本控制器补充的是 Server 维度的管理能力。
 */
@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    private final McpService mcpService;
    private final PluginGovernanceService pluginGovernanceService;

    public McpController(McpService mcpService, PluginGovernanceService pluginGovernanceService) {
        this.mcpService = mcpService;
        this.pluginGovernanceService = pluginGovernanceService;
    }

    /**
     * 列出当前已发现的 MCP Server。
     */
    @GetMapping("/servers")
    public ApiResult<List<McpServerVO>> listServers() {
        List<McpServerVO> servers = new ArrayList<>();
        for (McpService.McpServerInfo server : mcpService.getServers()) {
            servers.add(new McpServerVO(
                    server.getName(),
                    "mcp",
                    server.getEndpoint(),
                    server.getDescription(),
                    server.getTools() == null ? 0 : server.getTools().size()
            ));
        }
        return ApiResult.success(servers);
    }

    /**
     * 注销指定 MCP Server。
     */
    @DeleteMapping("/servers/{serverName}")
    public ApiResult<String> unregisterServer(@PathVariable String serverName) {
        boolean removed = mcpService.unregisterServer(serverName);
        if (removed) {
            log.info("MCP Server 已注销: {}", serverName);
            return ApiResult.success("Server 已注销: " + serverName);
        }
        return ApiResult.error("Server 不存在: " + serverName);
    }

    /**
     * 登记插件包（读取元信息，不做类加载）。
     *
     * <p>请求体：{@code {"jarPath": "./plugins/xxx-plugin.jar"}}
     */
    @PostMapping("/plugins/load")
    public ApiResult<PluginMetadata> loadPlugin(@RequestBody(required = false) Map<String, String> request) {
        String jarPath = request == null ? null : request.get("jarPath");

        try {
            PluginMetadata plugin = pluginGovernanceService.loadPlugin(jarPath);
            return ApiResult.success("插件加载成功: " + plugin.getName() + " (" + plugin.getVersion() + ")", plugin);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("插件加载失败: {}", e.getMessage());
            return ApiResult.error("插件加载失败: " + e.getMessage());
        }
    }

    /**
     * 执行指定 MCP 工具。
     *
     * <p>请求体直接是工具的入参对象。
     */
    @PostMapping("/tools/{toolName}/execute")
    public ApiResult<Map<String, Object>> executeTool(@PathVariable String toolName,
                                                      @RequestBody(required = false) Map<String, Object> args) {
        log.info("执行 MCP 工具: {}，参数: {}", toolName, args);

        String output = mcpService.executeTool(toolName, args == null ? Map.of() : args);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("toolName", toolName);
        data.put("content", output == null ? "" : output);
        return ApiResult.success(data);
    }

    /**
     * MCP Server 视图对象。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpServerVO {
        private String name;
        private String type;
        private String endpoint;
        private String description;
        private int toolCount;
    }
}

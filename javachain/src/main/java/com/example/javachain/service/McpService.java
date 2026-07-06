package com.example.javachain.service;

import com.example.javachain.config.McpClientConfig.McpServiceDiscoverer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class McpService {

    private static final int CONNECT_TIMEOUT_MS = (int) Duration.ofSeconds(5).toMillis();
    private static final int READ_TIMEOUT_MS = (int) Duration.ofSeconds(20).toMillis();

    private final ObjectMapper objectMapper;
    private final McpServiceDiscoverer mcpServiceDiscoverer;
    private final DiscoveryClient discoveryClient;
    private final SkillService skillService;
    private final Map<String, McpServerInfo> mcpServers = new ConcurrentHashMap<>();

    @Value("${mcp.target-service:mysql-mcp-server}")
    private String targetServiceName;

    @Value("${mcp.sse-endpoint:/mcp/sse}")
    private String sseEndpointPath;

    @Value("${mcp.fallback-base-url:http://127.0.0.1:8083}")
    private String fallbackBaseUrl;

    public McpService(ObjectMapper objectMapper,
                      McpServiceDiscoverer mcpServiceDiscoverer,
                      DiscoveryClient discoveryClient,
                      SkillService skillService) {
        this.objectMapper = objectMapper;
        this.mcpServiceDiscoverer = mcpServiceDiscoverer;
        this.discoveryClient = discoveryClient;
        this.skillService = skillService;
    }

    @PostConstruct
    public void init() {
        refreshMcpServers();
        log.info("McpService initialized, target MCP service: {}", targetServiceName);
    }

    @PreDestroy
    public void destroy() {
        log.info("McpService closed");
    }

    public List<McpToolInfo> getToolsFromServer(String serverName) {
        refreshMcpServers();
        McpServerInfo server = mcpServers.get(serverName);
        if (server == null) {
            log.warn("MCP server not found: {}", serverName);
            return Collections.emptyList();
        }
        return ensureToolsLoaded(server);
    }

    public List<McpToolInfo> getAllAvailableTools() {
        refreshMcpServers();
        List<McpToolInfo> allTools = new ArrayList<>();
        for (McpServerInfo server : mcpServers.values()) {
            allTools.addAll(ensureToolsLoaded(server));
        }
        allTools.addAll(getLocalTools());
        return allTools;
    }

    public String getAvailableTools() {
        try {
            return objectMapper.writeValueAsString(getAllAvailableTools());
        } catch (Exception e) {
            log.error("Failed to get MCP tool list", e);
            return "[]";
        }
    }

    public String executeTool(String toolName, Map<String, Object> arguments) {
        try {
            LocalToolMatch localTool = findLocalTool(toolName);
            if (localTool != null) {
                return executeLocalTool(localTool.method(), arguments);
            }

            refreshMcpServers();
            McpToolMatch match = findTool(toolName);
            if (match == null) {
                return String.format("Tool not found: %s. Please ensure %s is registered in Nacos and running.",
                        toolName, targetServiceName);
            }

            Map<String, Object> request = new LinkedHashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", UUID.randomUUID().toString());
            request.put("method", "tools/call");

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("name", match.tool().getName());
            params.put("arguments", arguments != null ? arguments : Map.of());
            request.put("params", params);

            JsonNode response = sendMcpRequest(match.server().getEndpoint(), request);
            return extractToolCallResult(response);
        } catch (Exception e) {
            log.error("Failed to execute MCP tool: {}", toolName, e);
            return String.format("Tool execution failed: %s", e.getMessage());
        }
    }

    private List<McpToolInfo> getLocalTools() {
        List<McpToolInfo> localTools = new ArrayList<>();
        for (Method method : skillService.getClass().getMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool == null) {
                continue;
            }

            McpToolInfo toolInfo = new McpToolInfo(getLocalToolName(method, tool), getLocalToolDescription(tool));
            toolInfo.setInputSchema(buildLocalToolInputSchema(method));
            localTools.add(toolInfo);
        }
        return localTools;
    }

    private LocalToolMatch findLocalTool(String requestedToolName) {
        String normalizedRequest = normalizeToolName(requestedToolName);
        String camelCaseRequest = snakeToLowerCamel(requestedToolName);
        for (Method method : skillService.getClass().getMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool == null) {
                continue;
            }

            String toolName = getLocalToolName(method, tool);
            if (Objects.equals(normalizeToolName(toolName), normalizedRequest)
                    || Objects.equals(toolName, camelCaseRequest)) {
                return new LocalToolMatch(method);
            }
        }
        return null;
    }

    private String executeLocalTool(Method method, Map<String, Object> arguments) throws Exception {
        Object[] args = buildLocalToolArguments(method, arguments != null ? arguments : Map.of());
        try {
            Object result = method.invoke(skillService, args);
            return result == null ? "" : String.valueOf(result);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new IllegalStateException(cause);
        }
    }

    private Object[] buildLocalToolArguments(Method method, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = arguments.get(parameter.getName());
            if (value == null && parameters.length == 1 && arguments.size() == 1) {
                value = arguments.values().iterator().next();
            }
            args[i] = objectMapper.convertValue(value, parameter.getType());
        }
        return args;
    }

    private String getLocalToolName(Method method, Tool tool) {
        return tool.name() == null || tool.name().isBlank() ? method.getName() : tool.name();
    }

    private String getLocalToolDescription(Tool tool) {
        return tool.value() == null || tool.value().length == 0 ? "" : String.join("\n", tool.value());
    }

    private Map<String, Object> buildLocalToolInputSchema(Method method) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", toJsonSchemaType(parameter.getType()));
            properties.put(parameter.getName(), property);
            if (isRequiredLocalParameter(method, parameter)) {
                required.add(parameter.getName());
            }
        }

        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    private boolean isRequiredLocalParameter(Method method, Parameter parameter) {
        return !("getCurrentUserFromToken".equals(method.getName()) && "token".equals(parameter.getName()));
    }

    private String toJsonSchemaType(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        if (Number.class.isAssignableFrom(type)
                || type == byte.class || type == short.class || type == int.class || type == long.class
                || type == float.class || type == double.class) {
            return "number";
        }
        if (Map.class.isAssignableFrom(type)) {
            return "object";
        }
        if (List.class.isAssignableFrom(type) || type.isArray()) {
            return "array";
        }
        return "string";
    }

    public void refreshMcpServers() {
        if (!mcpServiceDiscoverer.isRegistered(targetServiceName)) {
            mcpServers.remove(targetServiceName);
            log.warn("MCP service is not registered or active in Nacos MCP Registry: {}", targetServiceName);
            return;
        }

        String endpoint = resolveSseEndpoint();
        McpServerInfo server = mcpServers.computeIfAbsent(targetServiceName, McpServerInfo::new);
        server.setEndpoint(endpoint);
        server.setDescription(targetServiceName);
        log.debug("Discovered MCP service {} at {}", targetServiceName, endpoint);
    }

    public void registerMcpServer(McpServerInfo server) {
        mcpServers.put(server.getName(), server);
        log.info("Registered MCP server manually: {}", server.getName());
    }

    private String buildSseEndpoint(ServiceInstance instance) {
        String base = instance.getUri().toString();
        String path = sseEndpointPath.startsWith("/") ? sseEndpointPath : "/" + sseEndpointPath;
        return base + path;
    }

    private String resolveSseEndpoint() {
        List<ServiceInstance> instances = discoveryClient.getInstances(targetServiceName);
        if (instances != null && !instances.isEmpty()) {
            ServiceInstance instance = instances.get(0);
            return buildSseEndpoint(instance);
        }

        String base = fallbackBaseUrl.endsWith("/")
                ? fallbackBaseUrl.substring(0, fallbackBaseUrl.length() - 1)
                : fallbackBaseUrl;
        String path = sseEndpointPath.startsWith("/") ? sseEndpointPath : "/" + sseEndpointPath;
        log.warn("No Nacos discovery instance found for {}, using fallback MCP URL: {}", targetServiceName, base);
        return base + path;
    }

    private List<McpToolInfo> ensureToolsLoaded(McpServerInfo server) {
        if (!server.getTools().isEmpty()) {
            return server.getTools();
        }

        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", UUID.randomUUID().toString());
            request.put("method", "tools/list");
            request.put("params", Map.of());

            JsonNode response = sendMcpRequest(server.getEndpoint(), request);
            JsonNode toolsNode = response.path("result").path("tools");
            if (!toolsNode.isArray()) {
                log.warn("MCP tools/list returned no tools: {}", response);
                return server.getTools();
            }

            List<McpToolInfo> tools = new ArrayList<>();
            for (JsonNode toolNode : toolsNode) {
                McpToolInfo tool = new McpToolInfo(
                        toolNode.path("name").asText(),
                        toolNode.path("description").asText()
                );
                if (toolNode.has("inputSchema")) {
                    tool.setInputSchema(objectMapper.convertValue(toolNode.get("inputSchema"), Map.class));
                }
                tools.add(tool);
            }
            server.setTools(tools);
            return tools;
        } catch (Exception e) {
            log.warn("Failed to load MCP tools from {}: {}", server.getName(), e.getMessage());
            return server.getTools();
        }
    }

    private McpToolMatch findTool(String requestedToolName) {
        String normalizedRequest = normalizeToolName(requestedToolName);
        String camelCaseRequest = snakeToLowerCamel(requestedToolName);

        for (McpServerInfo server : mcpServers.values()) {
            for (McpToolInfo tool : ensureToolsLoaded(server)) {
                if (Objects.equals(normalizeToolName(tool.getName()), normalizedRequest)
                        || Objects.equals(tool.getName(), camelCaseRequest)) {
                    return new McpToolMatch(server, tool);
                }
            }
        }

        McpServerInfo fallbackServer = mcpServers.get(targetServiceName);
        if (fallbackServer != null && requestedToolName.startsWith("mysql_")) {
            return new McpToolMatch(fallbackServer, new McpToolInfo(snakeToLowerCamel(requestedToolName), ""));
        }
        return null;
    }

    private JsonNode sendMcpRequest(String sseEndpoint, Map<String, Object> request) throws Exception {
        HttpURLConnection sseConnection = openSseConnection(sseEndpoint);
        try (InputStream inputStream = sseConnection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            SseEvent endpointEvent = readEvent(reader);
            while (endpointEvent != null && !"endpoint".equals(endpointEvent.event())) {
                endpointEvent = readEvent(reader);
            }
            if (endpointEvent == null || endpointEvent.data().isBlank()) {
                throw new IllegalStateException("MCP SSE endpoint event was not received");
            }

            URI messageUri = URI.create(sseEndpoint).resolve(endpointEvent.data());
            initializeMcpSession(reader, messageUri.toString());
            postJson(messageUri.toString(), request);

            String requestId = String.valueOf(request.get("id"));
            return readJsonRpcResponse(reader, requestId);
        } finally {
            sseConnection.disconnect();
        }
    }

    private void initializeMcpSession(BufferedReader reader, String messageUrl) throws Exception {
        String initializeId = UUID.randomUUID().toString();

        Map<String, Object> initializeRequest = new LinkedHashMap<>();
        initializeRequest.put("jsonrpc", "2.0");
        initializeRequest.put("id", initializeId);
        initializeRequest.put("method", "initialize");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", Map.of());
        params.put("clientInfo", Map.of(
                "name", "javachain",
                "version", "1.0.0"
        ));
        initializeRequest.put("params", params);

        postJson(messageUrl, initializeRequest);
        readJsonRpcResponse(reader, initializeId);

        Map<String, Object> initializedNotification = new LinkedHashMap<>();
        initializedNotification.put("jsonrpc", "2.0");
        initializedNotification.put("method", "notifications/initialized");
        initializedNotification.put("params", Map.of());
        postJson(messageUrl, initializedNotification);
    }

    private JsonNode readJsonRpcResponse(BufferedReader reader, String requestId) throws Exception {
        SseEvent responseEvent = readEvent(reader);
        while (responseEvent != null) {
            if (!responseEvent.data().isBlank()) {
                JsonNode response = objectMapper.readTree(responseEvent.data());
                if (requestId.equals(response.path("id").asText())) {
                    if (response.hasNonNull("error")) {
                        throw new IllegalStateException(response.path("error").path("message").asText(response.toString()));
                    }
                    return response;
                }
            }
            responseEvent = readEvent(reader);
        }
        throw new IllegalStateException("MCP response was not received");
    }

    private HttpURLConnection openSseConnection(String sseEndpoint) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(sseEndpoint).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/event-stream");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.connect();
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Failed to open MCP SSE connection, status: " + status);
        }
        return connection;
    }

    private void postJson(String url, Map<String, Object> body) throws Exception {
        byte[] payload = objectMapper.writeValueAsBytes(body);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(payload);
        }

        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Failed to post MCP message, status: " + status);
        }
        connection.disconnect();
    }

    private SseEvent readEvent(BufferedReader reader) throws Exception {
        String event = "message";
        StringBuilder data = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                if (data.length() > 0) {
                    return new SseEvent(event, data.toString());
                }
                event = "message";
                continue;
            }
            if (line.startsWith("event:")) {
                event = line.substring("event:".length()).trim();
            } else if (line.startsWith("data:")) {
                if (data.length() > 0) {
                    data.append('\n');
                }
                data.append(line.substring("data:".length()).trim());
            }
        }
        return null;
    }

    private String extractToolCallResult(JsonNode response) {
        JsonNode result = response.path("result");
        JsonNode content = result.path("content");
        if (content.isArray()) {
            List<String> parts = new ArrayList<>();
            for (JsonNode item : content) {
                if (item.has("text")) {
                    parts.add(item.path("text").asText());
                } else {
                    parts.add(item.toString());
                }
            }
            return String.join("\n", parts);
        }
        if (content.isTextual()) {
            return content.asText();
        }
        if (!result.isMissingNode() && !result.isNull()) {
            return result.toString();
        }
        return response.toString();
    }

    private String normalizeToolName(String value) {
        return value == null ? "" : value.replace("_", "").replace("-", "").toLowerCase();
    }

    private String snakeToLowerCamel(String value) {
        if (value == null || !value.contains("_")) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        String[] parts = value.split("_");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isBlank()) {
                continue;
            }
            String lower = parts[i].toLowerCase();
            if (builder.length() == 0) {
                builder.append(lower);
            } else {
                builder.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
            }
        }
        return builder.toString();
    }

    private record SseEvent(String event, String data) {
    }

    private record McpToolMatch(McpServerInfo server, McpToolInfo tool) {
    }

    private record LocalToolMatch(Method method) {
    }

    public static class McpServerInfo {
        private String name;
        private String endpoint;
        private String version;
        private String description;
        private List<McpToolInfo> tools = new ArrayList<>();

        public McpServerInfo() {
        }

        public McpServerInfo(String name) {
            this.name = name;
        }

        public McpServerInfo(String name, String endpoint, String version, String description) {
            this.name = name;
            this.endpoint = endpoint;
            this.version = version;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<McpToolInfo> getTools() {
            return tools;
        }

        public void setTools(List<McpToolInfo> tools) {
            this.tools = tools != null ? tools : new ArrayList<>();
        }
    }

    public static class McpToolInfo {
        private String name;
        private String description;
        private Map<String, Object> inputSchema = new HashMap<>();

        public McpToolInfo() {
        }

        public McpToolInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getInputSchema() {
            return inputSchema;
        }

        public void setInputSchema(Map<String, Object> inputSchema) {
            this.inputSchema = inputSchema != null ? inputSchema : new HashMap<>();
        }
    }
}

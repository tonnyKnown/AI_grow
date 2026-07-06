package com.example.javachain.service;

import com.example.javachain.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class IntelligentChatService {

    // Number of recent chat messages passed to the model as conversational context.
    private static final int MAX_HISTORY_MESSAGES = 12;

    // Maximum think-act-observe cycles for one /simple/with-history request.
    // This lets the model inspect tool results and decide whether another tool call is needed.
    private static final int MAX_REASONING_ROUNDS = 4;

    // Maximum tool calls the model can request in a single reasoning round.
    private static final int MAX_TOOL_CALLS_PER_ROUND = 3;

    // Hard cap for all tool calls in one request to avoid long-running or looping executions.
    private static final int MAX_TOTAL_TOOL_CALLS = 10;

    // Maximum characters kept from each tool result before passing it back to the model.
    private static final int MAX_TOOL_RESULT_CHARS = 6000;

    private final ChatLanguageModel chatLanguageModel;
    private final ChatHistoryService chatHistoryService;
    private final McpService mcpService;
    private final ObjectMapper objectMapper;

    public IntelligentChatService(ChatLanguageModel chatLanguageModel,
                                  ChatHistoryService chatHistoryService,
                                  McpService mcpService,
                                  ObjectMapper objectMapper) {
        this.chatLanguageModel = chatLanguageModel;
        this.chatHistoryService = chatHistoryService;
        this.mcpService = mcpService;
        this.objectMapper = objectMapper;
    }

    public String chatWithHistory(String sessionId, String message) {
        List<ChatMessage> previousHistory = new ArrayList<>(chatHistoryService.getHistory(sessionId));
        log.info("Intelligent chat started, sessionId={}, historySize={}, messageLength={}",
                sessionId, previousHistory.size(), message == null ? 0 : message.length());
        chatHistoryService.addMessage(sessionId, "user", message);

        try {
            String toolsJson = mcpService.getAvailableTools();
            log.info("MCP tools loaded for intelligent chat, sessionId={}, toolsJsonLength={}",
                    sessionId, toolsJson == null ? 0 : toolsJson.length());

            ReasoningContext context = runReasoningLoop(sessionId, previousHistory, message, toolsJson);
            String answer = buildFinalAnswer(previousHistory, message, toolsJson, context);
            chatHistoryService.addMessage(sessionId, "assistant", answer);
            log.info("Intelligent chat completed, sessionId={}, rounds={}, toolExecutions={}, answerLength={}",
                    sessionId, context.plans.size(), context.executions.size(), answer == null ? 0 : answer.length());
            return answer;
        } catch (Exception e) {
            log.error("Intelligent chat failed, sessionId={}", sessionId, e);
            String fallback = chatLanguageModel.generate(buildFallbackPrompt(previousHistory, message));
            chatHistoryService.addMessage(sessionId, "assistant", fallback);
            log.info("Intelligent chat fallback completed, sessionId={}, answerLength={}",
                    sessionId, fallback == null ? 0 : fallback.length());
            return fallback;
        }
    }

    private ReasoningContext runReasoningLoop(String sessionId,
                                              List<ChatMessage> history,
                                              String message,
                                              String toolsJson) throws Exception {
        ReasoningContext context = new ReasoningContext();
        Set<String> executedSignatures = new HashSet<>();

        for (int round = 1; round <= MAX_REASONING_ROUNDS; round++) {
            int remainingToolCalls = MAX_TOTAL_TOOL_CALLS - context.executions.size();
            if (remainingToolCalls <= 0) {
                context.stopReason = "Reached max total tool calls";
                break;
            }

            Plan plan = buildPlan(history, message, toolsJson, context, round, remainingToolCalls);
            context.plans.add(plan);
            log.info("Reasoning round planned, sessionId={}, round={}, finalReady={}, steps={}, toolCalls={}, directAnswerLength={}, thoughtLength={}",
                    sessionId, round, plan.finalReady, plan.steps.size(), formatToolCallsForLog(plan.toolCalls),
                    plan.directAnswer == null ? 0 : plan.directAnswer.length(),
                    plan.thought == null ? 0 : plan.thought.length());

            if (plan.toolCalls.isEmpty()) {
                context.stopReason = plan.finalReady ? "Model marked final ready" : "No further tool calls planned";
                break;
            }

            List<ToolExecution> roundExecutions = executeTools(plan, round, executedSignatures, remainingToolCalls);
            context.executions.addAll(roundExecutions);
            log.info("Reasoning round tools executed, sessionId={}, round={}, executionCount={}, totalExecutions={}",
                    sessionId, round, roundExecutions.size(), context.executions.size());

            if (roundExecutions.isEmpty()) {
                context.stopReason = "No new tool execution";
                break;
            }

            if (plan.finalReady) {
                context.stopReason = "Model marked final ready after tool execution";
                break;
            }

            if (round == MAX_REASONING_ROUNDS) {
                context.stopReason = "Reached max reasoning rounds";
            }
        }

        if (context.stopReason == null || context.stopReason.isBlank()) {
            context.stopReason = "Reasoning loop completed";
        }
        return context;
    }

    private Plan buildPlan(List<ChatMessage> history,
                           String message,
                           String toolsJson,
                           ReasoningContext context,
                           int round,
                           int remainingToolCalls) throws Exception {
        String prompt = """
                You are a company business assistant. Think step by step, inspect previous tool results,
                then decide whether another tool call is needed before giving the final answer.

                Available MCP tools JSON:
                %s

                Recent chat history:
                %s

                User question:
                %s

                Previous reasoning plans JSON:
                %s

                Previous tool execution results JSON:
                %s

                Current reasoning round: %d
                Remaining total tool calls: %d

                Return only one JSON object. Do not use markdown.
                JSON schema:
                {
                  "thought": "Your reasoning about the question and previous tool results.",
                  "observation": "What you learned from previous tool results. Empty if no tool result yet.",
                  "steps": ["Step 1", "Step 2"],
                  "finalReady": false,
                  "toolCalls": [
                    {
                      "toolName": "Tool name from the available tools JSON",
                      "arguments": {"argName": "argValue"}
                    }
                  ],
                  "directAnswer": "Only fill this when finalReady is true and no more tool calls are needed."
                }

                Rules:
                1. Respond in Chinese in all natural-language fields.
                2. If the previous tool result is insufficient, wrong table, empty, or ambiguous, plan another useful tool call.
                3. If you need database schema, call mysqlListTables or mysqlDescribeTable before writing uncertain SQL.
                4. Prefer read-only tools such as mysqlQuery, mysqlListTables, mysqlDescribeTable.
                5. Do not use write tools unless the user explicitly asks to insert, update, or delete data.
                6. Set finalReady=true only when the current evidence is enough to answer accurately.
                7. Plan at most %d tool calls in this round, and never exceed remaining total tool calls.
                """.formatted(
                toolsJson,
                formatHistory(history),
                message,
                objectMapper.writeValueAsString(context.plans),
                objectMapper.writeValueAsString(context.executions),
                round,
                remainingToolCalls,
                Math.min(MAX_TOOL_CALLS_PER_ROUND, remainingToolCalls));

        String raw = chatLanguageModel.generate(prompt);
        JsonNode root = objectMapper.readTree(extractJsonObject(raw));

        Plan plan = new Plan();
        plan.thought = root.path("thought").asText("");
        plan.observation = root.path("observation").asText("");
        plan.directAnswer = root.path("directAnswer").asText("");
        plan.finalReady = root.path("finalReady").asBoolean(false);

        JsonNode stepsNode = root.path("steps");
        if (stepsNode.isArray()) {
            for (JsonNode stepNode : stepsNode) {
                String step = stepNode.asText("");
                if (!step.isBlank()) {
                    plan.steps.add(step);
                }
            }
        }

        int maxCallsThisRound = Math.min(MAX_TOOL_CALLS_PER_ROUND, remainingToolCalls);
        JsonNode toolCallsNode = root.path("toolCalls");
        if (toolCallsNode.isArray()) {
            for (JsonNode callNode : toolCallsNode) {
                if (plan.toolCalls.size() >= maxCallsThisRound) {
                    break;
                }
                String toolName = callNode.path("toolName").asText("");
                if (toolName.isBlank()) {
                    continue;
                }
                Map<String, Object> arguments = new LinkedHashMap<>();
                JsonNode argsNode = callNode.path("arguments");
                if (argsNode.isObject()) {
                    arguments = objectMapper.convertValue(argsNode, Map.class);
                }
                plan.toolCalls.add(new ToolCall(toolName, arguments));
            }
        }

        if (plan.steps.isEmpty()) {
            plan.steps.add("理解用户问题并检查已有工具结果");
            plan.steps.add(plan.toolCalls.isEmpty() ? "信息足够，整理最终回答" : "继续调用工具补充证据");
        }
        return plan;
    }

    private List<ToolExecution> executeTools(Plan plan,
                                             int round,
                                             Set<String> executedSignatures,
                                             int remainingToolCalls) throws Exception {
        List<ToolExecution> executions = new ArrayList<>();
        log.info("Executing tools with plan, round={}, thoughtLength={}, observationLength={}, steps={}, toolCalls={}, hasDirectAnswer={}",
                round,
                plan.thought == null ? 0 : plan.thought.length(),
                plan.observation == null ? 0 : plan.observation.length(),
                plan.steps,
                formatToolCallsForLog(plan.toolCalls),
                plan.directAnswer != null && !plan.directAnswer.isBlank());

        for (ToolCall call : plan.toolCalls) {
            if (executions.size() >= remainingToolCalls) {
                break;
            }

            String signature = call.toolName() + ":" + objectMapper.writeValueAsString(call.arguments());
            if (executedSignatures.contains(signature)) {
                log.info("Skipping duplicate MCP tool call, round={}, toolName={}, argumentKeys={}",
                        round, call.toolName(), call.arguments() == null ? List.of() : call.arguments().keySet());
                continue;
            }
            executedSignatures.add(signature);

            String result;
            try {
                log.info("Executing MCP tool, round={}, toolName={}, argumentKeys={}",
                        round, call.toolName(), call.arguments() == null ? List.of() : call.arguments().keySet());
                result = mcpService.executeTool(call.toolName(), call.arguments());
                log.info("MCP tool executed, round={}, toolName={}, resultLength={}",
                        round, call.toolName(), result == null ? 0 : result.length());
            } catch (Exception e) {
                log.error("Tool execution failed, round={}, toolName={}", round, call.toolName(), e);
                result = "工具执行失败: " + e.getMessage();
            }
            executions.add(new ToolExecution(round, call.toolName(), call.arguments(), limit(result, MAX_TOOL_RESULT_CHARS)));
        }
        return executions;
    }

    private String buildFinalAnswer(List<ChatMessage> history,
                                    String message,
                                    String toolsJson,
                                    ReasoningContext context) throws Exception {
        Plan lastPlan = context.plans.isEmpty() ? null : context.plans.get(context.plans.size() - 1);
        if (context.executions.isEmpty()
                && lastPlan != null
                && lastPlan.directAnswer != null
                && !lastPlan.directAnswer.isBlank()) {
            return """
                    ### 思考
                    %s

                    ### 解决步骤
                    %s

                    ### 最终回答
                    %s
                    """.formatted(
                    emptyToDefault(lastPlan.thought, "该问题不需要调用外部工具，可以直接回答。"),
                    formatSteps(lastPlan.steps),
                    lastPlan.directAnswer).trim();
        }

        String prompt = """
                You are a company business assistant. Produce the final answer based on the full reasoning trace
                and all tool execution results.

                Available MCP tools JSON:
                %s

                Recent chat history:
                %s

                User question:
                %s

                Reasoning plans JSON:
                %s

                Tool execution results JSON:
                %s

                Stop reason:
                %s

                Answer in Chinese with this structure:
                ### 思考
                Briefly explain how you understood the question and how you used the tool results.

                ### 解决步骤
                List the important steps you took, including follow-up tool calls when previous results were insufficient.

                ### 执行结果
                Summarize only facts supported by tool results. If a tool failed or returned empty data, say so clearly.

                ### 最终回答
                Give the accurate conclusion and practical next steps.

                Requirements:
                1. Do not invent data that is not present in tool results.
                2. If evidence is insufficient after all rounds, clearly state what is still missing.
                3. Do not expose raw JSON unless it is useful to the user.
                """.formatted(
                toolsJson,
                formatHistory(history),
                message,
                objectMapper.writeValueAsString(context.plans),
                objectMapper.writeValueAsString(context.executions),
                context.stopReason);

        return chatLanguageModel.generate(prompt).trim();
    }

    private String buildFallbackPrompt(List<ChatMessage> history, String message) {
        return """
                You are a company business assistant. The intelligent tool workflow failed.

                Recent chat history:
                %s

                User question:
                %s

                Answer in Chinese. If a tool query is required to confirm the answer, say that clearly.
                """.formatted(formatHistory(history), message);
    }

    private String formatHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return "无";
        }
        int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < history.size(); i++) {
            ChatMessage item = history.get(i);
            builder.append("user".equals(item.getRole()) ? "用户: " : "助手: ")
                    .append(item.getContent())
                    .append('\n');
        }
        return builder.toString().trim();
    }

    private String formatSteps(List<String> steps) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            builder.append(i + 1).append(". ").append(steps.get(i)).append('\n');
        }
        return builder.toString().trim();
    }

    private List<Map<String, Object>> formatToolCallsForLog(List<ToolCall> toolCalls) {
        List<Map<String, Object>> calls = new ArrayList<>();
        for (ToolCall call : toolCalls) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("toolName", call.toolName());
            item.put("argumentKeys", call.arguments() == null ? List.of() : call.arguments().keySet());
            calls.add(item);
        }
        return calls;
    }

    private String extractJsonObject(String raw) {
        if (raw == null) {
            return "{}";
        }
        String text = raw.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String limit(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "\n...结果过长，已截断...";
    }

    private static class ReasoningContext {
        private final List<Plan> plans = new ArrayList<>();
        private final List<ToolExecution> executions = new ArrayList<>();
        private String stopReason = "";
    }

    private static class Plan {
        private String thought = "";
        private String observation = "";
        private String directAnswer = "";
        private boolean finalReady;
        private final List<String> steps = new ArrayList<>();
        private final List<ToolCall> toolCalls = new ArrayList<>();

        public String getThought() {
            return thought;
        }

        public String getObservation() {
            return observation;
        }

        public String getDirectAnswer() {
            return directAnswer;
        }

        public boolean isFinalReady() {
            return finalReady;
        }

        public List<String> getSteps() {
            return steps;
        }

        public List<ToolCall> getToolCalls() {
            return toolCalls;
        }
    }

    private record ToolCall(String toolName, Map<String, Object> arguments) {
    }

    private record ToolExecution(int round, String toolName, Map<String, Object> arguments, String result) {
    }
}

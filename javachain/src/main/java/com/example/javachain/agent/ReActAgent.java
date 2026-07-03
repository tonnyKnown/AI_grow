package com.example.javachain.agent;

import com.example.javachain.config.ReActAgentConfig;
import com.example.javachain.service.McpService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct 推理 Agent
 * <p>
 * 实现 ReAct (Reasoning + Acting) 模式，支持多步骤工具调用和自主推理
 * <p>
 * 工作流程：
 * 1. Thought - 分析当前状态和目标
 * 2. Action - 选择合适的工具
 * 3. ActionInput - 准备工具参数
 * 4. Observation - 获取执行结果
 * 5. 重复直到任务完成
 * <p>
 * 安全特性：
 * - 最大步骤数限制（防止无限循环）
 * - 工具调用统计和限制
 * - 敏感操作安全检查
 * - 完整的错误处理和恢复
 * <p>
 * 优化特性：
 * - 配置化管理所有参数
 * - 工具列表缓存机制
 * - 工具调用重试机制
 * - 性能监控和指标统计
 */
@Slf4j
@Component
public class ReActAgent {

    private final ChatLanguageModel chatLanguageModel;
    private final McpService mcpService;
    private final ObjectMapper objectMapper;
    private final ReActAgentConfig config;

    // 工具列表缓存
    private String cachedToolsJson;
    private long cacheTimestamp = 0;

    // 性能指标
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalToolCalls = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalDurationMs = new AtomicLong(0);

    public ReActAgent(ChatLanguageModel chatLanguageModel,
                      McpService mcpService,
                      ObjectMapper objectMapper,
                      ReActAgentConfig config) {
        this.chatLanguageModel = chatLanguageModel;
        this.mcpService = mcpService;
        this.objectMapper = objectMapper;
        this.config = config;
        log.info("✅ ReActAgent 初始化完成，配置：maxSteps={}, timeoutMs={}, retryCount={}",
                config.getMaxSteps(), config.getTimeoutMs(), config.getRetryCount());
    }

    /**
     * 执行自主推理任务
     */
    public ReasoningResult execute(String userQuestion) {
        return executeWithCallback(userQuestion, null);
    }

    /**
     * 执行推理任务（带 JobContext）
     */
    public ReasoningResult executeJob(JobContext job) {
        return executeWithCallback(job.getQuestion(), (step, total) -> {
            job.addReasoningStep(step.toString());
        });
    }

    /**
     * 继续执行任务（用户确认后）
     */
    public ReasoningResult continueJob(JobContext job, boolean confirm) {
        if (!confirm) {
            return ReasoningResult.builder()
                    .success(false)
                    .errorMessage("用户已取消操作")
                    .question(job.getQuestion())
                    .build();
        }

        // 直接执行待确认的工具
        String toolName = job.getPendingToolName();
        String actionInput = null;
        try {
            actionInput = objectMapper.writeValueAsString(job.getPendingToolArgs());
        } catch (Exception e) {
            log.error("序列化参数失败", e);
        }

        String observation = executeTool(toolName, actionInput);
        
        ReasoningResult result = ReasoningResult.builder()
                .success(isObservationUseful(observation))
                .answer(isObservationUseful(observation) ? observation : "工具执行失败: " + observation)
                .question(job.getQuestion())
                .build();
        
        return result;
    }

    /**
     * 执行推理任务（带回调）
     */
    public ReasoningResult executeWithCallback(String userQuestion, StepCallback callback) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            log.info("═══════════════════════════════════════════════════════════");
            log.info("🔍 开始执行推理任务");
            log.info("   用户问题: {}", userQuestion);

            // 安全检查
            if (!validateSafety(userQuestion)) {
                String errorMsg = "检测到敏感内容，无法继续执行";
                log.warn("⚠️ {}", errorMsg);
                return ReasoningResult.builder().success(false).errorMessage(errorMsg).build();
            }

            // 初始化推理上下文
            ReasoningContext ctx = ReasoningContext.builder()
                    .systemPrompt(buildSystemPrompt())
                    .originalQuestion(userQuestion)
                    .currentState("")
                    .stepCount(0)
                    .toolCallCount(new ConcurrentHashMap<>())
                    .consecutiveFailures(0)
                    .hasUsefulObservation(false)
                    .startTime(System.currentTimeMillis())
                    .build();

            ReasoningResult result = ReasoningResult.builder()
                    .steps(new ArrayList<>())
                    .startTime(startTime)
                    .build();

            // 推理循环
            while (ctx.shouldContinue(config.getMaxSteps())) {
                ctx.incrementStep();

                // 构建提示词
                String prompt = buildPrompt(ctx);

                // 调用 LLM
                String response = chatLanguageModel.generate(prompt);

                // 解析响应
                ReasoningStep step = parseStep(response);
                step.setStepNumber(ctx.getStepCount());

                // 处理步骤结果
                if (handleStepResult(step, ctx, result, callback)) {
                    break;
                }

                // 执行工具调用
                if (!step.hasToolCall()) {
                    step.setStatus(ReasoningStep.StepStatus.FAILED);
                    step.setErrorMessage("无效的工具调用格式");
                    ctx.incrementFailures();
                    result.addStep(step);
                    continue;
                }

                // 安全检查：工具调用
                if (!validateToolCall(step.getAction(), step.getActionInput())) {
                    step.setStatus(ReasoningStep.StepStatus.FAILED);
                    step.setErrorMessage("工具调用参数包含敏感内容");
                    ctx.incrementFailures();
                    result.addStep(step);
                    continue;
                }

                // 检查是否连续调用同一工具
                if (ctx.isConsecutiveToolCall(step.getAction())) {
                    log.warn("⚠️ 检测到连续调用同一工具: {}", step.getAction());
                    step.setStatus(ReasoningStep.StepStatus.FAILED);
                    step.setErrorMessage("禁止连续调用同一工具");
                    ctx.incrementFailures();
                    result.addStep(step);
                    continue;
                }

                // 检查工具调用次数是否超限
                if (ctx.isToolCallExceeded(step.getAction(), config.getMaxToolCallsPerTool())) {
                    log.warn("⚠️ 工具调用次数超限: {}", step.getAction());
                    step.setStatus(ReasoningStep.StepStatus.FAILED);
                    step.setErrorMessage("工具调用次数已达上限");
                    ctx.incrementFailures();
                    result.addStep(step);
                    continue;
                }

                // 检查危险工具
                if (isDangerousTool(step.getAction())) {
                    log.warn("⚠️ 检测到危险工具调用: {}", step.getAction());
                    result.pending(step.getAction(), step.getActionInput(),
                            buildActionDescription(step.getAction(), step.getActionInput()));
                    result.addStep(step);
                    return result;
                }

                // 执行工具
                String observation = executeTool(step.getAction(), step.getActionInput());
                step.setObservation(observation);

                // 判断结果是否有用
                if (isObservationUseful(observation)) {
                    ctx.recordUsefulObservation(observation);
                    ctx.setConsecutiveFailures(0);
                    step.setStatus(ReasoningStep.StepStatus.COMPLETED);
                } else {
                    ctx.incrementFailures();
                    step.setStatus(ReasoningStep.StepStatus.FAILED);
                }

                // 更新上下文
                ctx.incrementToolCall(step.getAction());
                ctx.appendToState(step);

                result.addStep(step);

                // 回调通知
                if (callback != null) {
                    callback.onStep(step, config.getMaxSteps());
                }
            }

            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;
            totalDurationMs.addAndGet(duration);

            if (result.isSuccess()) {
                totalSuccess.incrementAndGet();
                log.info("✅ 推理任务完成");
                log.info("   总耗时: {}ms", duration);
                log.info("   执行步数: {}", ctx.getStepCount());
            } else {
                totalFailed.incrementAndGet();
                log.warn("⚠️ 推理任务失败");
            }
            log.info("═══════════════════════════════════════════════════════════");

            return result;

        } catch (Exception e) {
            totalFailed.incrementAndGet();
            log.error("❌ 推理任务异常", e);
            return ReasoningResult.builder()
                    .success(false).errorMessage("推理过程发生错误: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
                你是一个智能助手，能够通过调用工具来回答问题。

                ## 可用工具
                %s

                ## 任务说明
                你需要根据用户的问题，选择合适的工具来获取信息或执行操作。

                ## 格式要求
                你的回答必须严格遵循以下格式：

                1. **思考阶段** (Thought):
                   - 分析当前状态和目标
                   - 确定下一步行动

                2. **执行阶段** (仅当需要调用工具时):
                   Action: <工具名称>
                   ActionInput: <JSON格式的参数>

                3. **总结阶段** (当问题已解决时):
                   Final Answer: <最终答案>

                ## 重要规则
                - ❌ **禁止重复调用同一个工具！** 获取到工具结果后，必须基于该结果进行总结或尝试其他工具
                - ❌ **禁止循环调用！** 如果已经获取到某个工具的执行结果，应该总结答案或尝试其他方法
                - ⚠️ **必须使用工具获取信息！** 不要凭空猜测答案
                - ✅ **用中文回答！** 所有输出都必须使用中文

                ## 输出示例
                思考：用户想知道深圳明天的天气，我需要调用天气查询工具。
                Action: weather_forecast
                ActionInput: {"city": "深圳", "days": 1}

                ---

                思考：已经获取到天气信息，可以总结回答用户的问题了。
                Final Answer: 深圳明天的天气是晴转多云，气温25-32°C。
                """.formatted(buildToolsDescription());
    }

    /**
     * 动态构建工具描述（带缓存）
     */
    private String buildToolsDescription() {
        try {
            long now = System.currentTimeMillis();
            if (cachedToolsJson != null && (now - cacheTimestamp) < config.getToolCacheExpireMs()) {
                log.debug("使用缓存的工具列表（缓存时间: {}ms 前）", now - cacheTimestamp);
                return parseToolsJson(cachedToolsJson);
            }

            log.debug("缓存已过期或不存在，从 MCP 服务获取工具列表");
            String toolsJson = mcpService.getAvailableTools();
            
            cachedToolsJson = toolsJson;
            cacheTimestamp = now;

            return parseToolsJson(toolsJson);
            
        } catch (Exception e) {
            log.error("获取工具描述失败", e);
            return "获取工具列表失败";
        }
    }

    /**
     * 解析工具列表 JSON
     */
    private String parseToolsJson(String toolsJson) throws Exception {
        List<String> descriptions = new ArrayList<>();
        objectMapper.readTree(toolsJson).forEach(node -> {
            String name = node.get("name").asText();
            String desc = node.get("description").asText();
            StringBuilder params = new StringBuilder();
            node.get("parameters").forEach(param -> {
                if (params.length() > 0) params.append(", ");
                params.append(param.asText());
            });
            descriptions.add(String.format("- %s: %s\n  参数: %s", name, desc, params));
        });
        return descriptions.isEmpty() ? "无可用工具" : String.join("\n\n", descriptions);
    }

    /**
     * 刷新工具列表缓存
     */
    public void refreshToolCache() {
        log.info("手动刷新工具列表缓存");
        cachedToolsJson = null;
        cacheTimestamp = 0;
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(ReasoningContext ctx) {
        // 检查 token 限制
        int currentTokens = estimateTokens(ctx.getCurrentState());
        int maxTokens = config.getMaxPromptTokens();
        if (currentTokens > maxTokens * config.getSummarizationThreshold()) {
            ctx.truncateHistory(maxTokens);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("""
                系统提示：
                %s

                ---

                对话历史：
                """.formatted(ctx.getSystemPrompt()));

        if (!ctx.getCurrentState().isEmpty()) {
            sb.append(ctx.getCurrentState());
        }

        sb.append("\n\n");
        sb.append("用户问题：").append(ctx.getOriginalQuestion());
        sb.append("\n\n");
        sb.append("请根据以上信息进行推理，输出你的思考和行动。");

        return sb.toString();
    }

    /**
     * 估算 Token 数量
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chineseChars = text.length() - text.replaceAll("[\\u4e00-\\u9fff]", "").length();
        int otherChars = text.length() - chineseChars;
        return (int) (chineseChars * 1.5 + otherChars / 4.0);
    }

    /**
     * 解析 LLM 响应
     */
    private ReasoningStep parseStep(String response) {
        ReasoningStep step = ReasoningStep.builder().build();

        // 解析 Thought
        Pattern thoughtPattern = Pattern.compile("思考[：:]\\s*(.+?)(?=\\n\\s*Action|\\n\\s*Final|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher thoughtMatcher = thoughtPattern.matcher(response);
        if (thoughtMatcher.find()) {
            step.setThought(thoughtMatcher.group(1).trim());
        }

        // 解析 Action
        Pattern actionPattern = Pattern.compile("Action[：:]\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher actionMatcher = actionPattern.matcher(response);
        if (actionMatcher.find()) {
            step.setAction(actionMatcher.group(1).trim());
        }

        // 解析 ActionInput
        Pattern inputPattern = Pattern.compile("ActionInput[：:]\\s*(\\{.+?\\}|\\{.*)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher inputMatcher = inputPattern.matcher(response);
        if (inputMatcher.find()) {
            step.setActionInput(inputMatcher.group(1).trim());
        }

        // 检查是否是最终答案
        if (response.contains("Final Answer") || response.contains("最终答案")) {
            step.setFinalAnswer(true);
            Pattern answerPattern = Pattern.compile("(?:Final Answer|最终答案)[：:]\\s*(.+)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher answerMatcher = answerPattern.matcher(response);
            if (answerMatcher.find()) {
                step.setAnswerContent(answerMatcher.group(1).trim());
            }
        }

        return step;
    }

    /**
     * 执行工具（带重试机制）
     */
    private String executeTool(String toolName, String actionInput) {
        totalToolCalls.incrementAndGet();
        long startTime = System.currentTimeMillis();
        int retryCount = config.getRetryCount();
        long retryDelay = config.getRetryDelayMs();

        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                log.info("═══════════════════════════════════════════════════════════");
                log.info("🔧 开始执行工具 (第 {}/{} 次尝试)", attempt, retryCount + 1);
                log.info("   工具名称: {}", toolName);
                log.info("   输入参数: {}", actionInput);

                Map<String, Object> args = parseActionInput(actionInput);
                String result = mcpService.executeTool(toolName, args);

                long duration = System.currentTimeMillis() - startTime;
                totalDurationMs.addAndGet(duration);
                
                log.info("✅ 工具执行成功");
                log.info("   工具名称: {}", toolName);
                log.info("   执行耗时: {}ms", duration);
                log.info("   执行结果: {}", result);
                log.info("═══════════════════════════════════════════════════════════");

                return result;

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                if (attempt <= retryCount) {
                    log.warn("⚠️ 工具执行失败（第 {}/{} 次尝试），将在 {}ms 后重试", 
                            attempt, retryCount + 1, retryDelay);
                    log.warn("   工具名称: {}", toolName);
                    log.warn("   错误信息: {}", e.getMessage());
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("❌ 工具执行失败（已重试 {} 次）", retryCount);
                    log.error("   工具名称: {}", toolName);
                    log.error("   输入参数: {}", actionInput);
                    log.error("   总耗时: {}ms", duration);
                    log.error("   错误信息: {}", e.getMessage(), e);
                    return "工具执行失败：" + e.getMessage();
                }
            }
        }
        return "工具执行失败：重试次数已用尽";
    }

    /**
     * 解析 ActionInput
     */
    private Map<String, Object> parseActionInput(String actionInput) {
        try {
            return objectMapper.readValue(actionInput, Map.class);
        } catch (Exception e) {
            log.warn("解析 ActionInput 失败，尝试手动解析: {}", actionInput);
            Map<String, Object> result = new ConcurrentHashMap<>();
            try {
                String clean = actionInput.trim().replaceAll("[{}]", "");
                String[] pairs = clean.split(",");
                for (String pair : pairs) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        result.put(parts[0].trim().replace("\"", ""), 
                                parts[1].trim().replace("\"", ""));
                    }
                }
            } catch (Exception ex) {
                log.error("手动解析也失败", ex);
            }
            return result;
        }
    }

    /**
     * 判断观察结果是否有用
     */
    private boolean isObservationUseful(String observation) {
        if (observation == null || observation.trim().isEmpty()) {
            return false;
        }

        String lowerObservation = observation.toLowerCase().trim();
        String[] errorKeywords = {
            "失败", "错误", "exception", "error", "failed", 
            "invalid", "timeout", "连接超时", "网络错误", 
            "服务不可用", "拒绝访问", "权限不足", "not found"
        };

        for (String keyword : errorKeywords) {
            if (lowerObservation.contains(keyword.toLowerCase())) {
                log.debug("检测到错误关键词: {}", keyword);
                return false;
            }
        }

        try {
            JsonNode node = objectMapper.readTree(observation);
            if (node.has("success") && !node.get("success").asBoolean()) {
                log.debug("JSON响应中 success=false");
                return false;
            }
            if (node.has("code") && node.get("code").asInt() != 200) {
                log.debug("JSON响应中 code != 200: {}", node.get("code").asInt());
                return false;
            }
        } catch (Exception e) {
            // 不是JSON格式
        }

        String[] emptyKeywords = {"无数据", "空", "null", "undefined"};
        String trimmedObs = observation.trim();
        for (String keyword : emptyKeywords) {
            if (trimmedObs.equalsIgnoreCase(keyword) || trimmedObs.equals("{}") || trimmedObs.equals("[]")) {
                return false;
            }
        }

        return true;
    }

    /**
     * 处理步骤结果
     */
    private boolean handleStepResult(ReasoningStep step, ReasoningContext ctx, 
                                     ReasoningResult result, StepCallback callback) {
        result.addStep(step);

        if (step.isFinalAnswer()) {
            result.success(step.getAnswerContent());
            log.info("✅ 任务完成，共执行 {} 步", ctx.getStepCount());
            return true;
        }

        if (ctx.isConsecutiveFailureExceeded(config.getMaxConsecutiveFailures())) {
            return handleConsecutiveFailures(ctx, result);
        }

        if (ctx.isTimedOut(config.getTimeoutMs())) {
            return handleTimeout(ctx, result);
        }

        return false;
    }

    /**
     * 处理连续失败
     */
    private boolean handleConsecutiveFailures(ReasoningContext ctx, ReasoningResult result) {
        log.warn("⚠️ 连续失败次数已达上限");
        if (ctx.isHasUsefulObservation()) {
            String summary = tryToSummarize(ctx);
            result.success("由于多次工具调用失败，根据已获取的信息总结如下：\n" + summary);
        } else {
            result.failed("连续失败次数已达上限，无法完成任务");
        }
        return true;
    }

    /**
     * 处理超时
     */
    private boolean handleTimeout(ReasoningContext ctx, ReasoningResult result) {
        log.warn("⚠️ 推理超时");
        if (ctx.isHasUsefulObservation()) {
            String summary = tryToSummarize(ctx);
            result.success("由于推理时间过长，根据已获取的信息总结如下：\n" + summary);
        } else {
            result.failed("推理超时，无法完成任务");
        }
        return true;
    }

    /**
     * 尝试总结现有信息
     */
    private String tryToSummarize(ReasoningContext ctx) {
        try {
            String prompt = """
                    根据以下对话历史，总结对用户问题的回答：
                    
                    用户问题：%s
                    
                    对话历史：
                    %s
                    
                    请用简洁的语言总结回答。
                    """.formatted(ctx.getOriginalQuestion(), ctx.getCurrentState());
            
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.error("总结失败", e);
            return "无法生成总结";
        }
    }

    /**
     * 安全检查：问题
     */
    private boolean validateSafety(String question) {
        String lowerQuestion = question.toLowerCase();
        for (String keyword : config.getSensitiveKeywords()) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                log.warn("⚠️ 检测到敏感关键词: {}", keyword);
                return false;
            }
        }
        return true;
    }

    /**
     * 安全检查：工具调用
     */
    private boolean validateToolCall(String toolName, String actionInput) {
        if (toolName == null || toolName.trim().isEmpty()) {
            return false;
        }

        String lowerInput = actionInput.toLowerCase();
        for (String keyword : config.getSensitiveKeywords()) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                log.warn("⚠️ 工具参数包含敏感关键词: {}", keyword);
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否是危险工具
     */
    private boolean isDangerousTool(String toolName) {
        return config.getDangerousTools().contains(toolName);
    }

    /**
     * 构建操作描述
     */
    private String buildActionDescription(String action, String actionInput) {
        return String.format("工具：%s\n参数：%s", action, actionInput);
    }

    /**
     * 获取性能指标
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("totalToolCalls", totalToolCalls.get());
        metrics.put("totalSuccess", totalSuccess.get());
        metrics.put("totalFailed", totalFailed.get());
        metrics.put("totalDurationMs", totalDurationMs.get());
        if (totalRequests.get() > 0) {
            metrics.put("avgDurationMs", totalDurationMs.get() / totalRequests.get());
        }
        return metrics;
    }

    /**
     * 步骤回调接口
     */
    public interface StepCallback {
        void onStep(ReasoningStep step, int total);
    }

    /**
     * 确认回调接口
     */
    public interface ConfirmationCallback {
        void onPending(String toolName, String actionInput, String description);
    }

    /**
     * 推理结果
     */
    @Data
    @lombok.Builder
    public static class ReasoningResult {
        private List<ReasoningStep> steps;
        private boolean success;
        private String answer;
        private String errorMessage;
        private long startTime;
        private long endTime;
        private String question;

        private boolean pendingConfirmation;
        private String pendingTool;
        private String pendingActionInput;
        private String pendingDescription;

        public void addStep(ReasoningStep step) {
            this.steps.add(step);
        }

        public ReasoningResult success(String answer) {
            this.success = true;
            this.answer = answer;
            this.endTime = System.currentTimeMillis();
            return this;
        }

        public ReasoningResult failed(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
            this.endTime = System.currentTimeMillis();
            return this;
        }

        public ReasoningResult pending(String toolName, String actionInput, String description) {
            this.pendingConfirmation = true;
            this.pendingTool = toolName;
            this.pendingActionInput = actionInput;
            this.pendingDescription = description;
            this.endTime = System.currentTimeMillis();
            return this;
        }

        public boolean hasPendingAction() {
            return pendingConfirmation && pendingTool != null;
        }

        public long getDuration() {
            return endTime > 0 ? endTime - startTime : 0;
        }
    }

    /**
     * 推理上下文
     */
    @lombok.Data
    @lombok.Builder
    private static class ReasoningContext {
        private String systemPrompt;
        private String currentState;
        private String originalQuestion;
        private int stepCount;
        private Map<String, Integer> toolCallCount;
        private int consecutiveFailures;
        private boolean hasUsefulObservation;
        private long startTime;
        private String lastToolCalled;

        public boolean shouldContinue(int maxSteps) {
            return stepCount < maxSteps;
        }

        public void incrementStep() {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            stepCount++;
        }

        public void incrementFailures() {
            consecutiveFailures++;
        }

        public void incrementToolCall(String toolName) {
            toolCallCount.merge(toolName, 1, Integer::sum);
            this.lastToolCalled = toolName;
        }

        public boolean isConsecutiveToolCall(String toolName) {
            return toolName != null && toolName.equals(lastToolCalled);
        }

        public boolean isToolCallExceeded(String toolName, int maxToolCallsPerTool) {
            return toolCallCount.getOrDefault(toolName, 0) >= maxToolCallsPerTool;
        }

        public boolean isConsecutiveFailureExceeded(int maxConsecutiveFailures) {
            return consecutiveFailures >= maxConsecutiveFailures;
        }

        public boolean isTimedOut(long timeoutMs) {
            return System.currentTimeMillis() - startTime > timeoutMs;
        }

        public void recordUsefulObservation(String observation) {
            hasUsefulObservation = true;
        }

        public void appendToState(ReasoningStep step) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n---\n");
            sb.append("步骤 ").append(step.getStepNumber()).append(":\n");
            sb.append("思考: ").append(step.getThought()).append("\n");
            if (step.hasToolCall()) {
                sb.append("Action: ").append(step.getAction()).append("\n");
                sb.append("ActionInput: ").append(step.getActionInput()).append("\n");
                sb.append("Observation: ").append(step.getObservation()).append("\n");
            } else if (step.isFinalAnswer()) {
                sb.append("Final Answer: ").append(step.getAnswerContent()).append("\n");
            }
            currentState += sb.toString();
        }

        /**
         * 截断历史（当 token 超限时）
         * 保留系统提示和最近 N 条步骤
         */
        public void truncateHistory(int maxTokens) {
            int tokens = estimateTokens(currentState);
            if (tokens <= maxTokens) return;

            // 简单策略：保留最近 5 条步骤
            int maxStepsToKeep = 5;
            String[] parts = currentState.split("---\n");
            if (parts.length <= maxStepsToKeep + 1) return;

            // 保留系统提示 + 最近步骤
            StringBuilder truncated = new StringBuilder(parts[0]); // 系统提示
            for (int i = parts.length - maxStepsToKeep; i < parts.length; i++) {
                truncated.append("---\n").append(parts[i]);
            }
            this.currentState = truncated.toString();
            log.debug("历史已截断，从 {} tokens 降到 {} tokens", tokens, estimateTokens(currentState));
        }

        private int estimateTokens(String text) {
            // 简单估算：中文按 1.5 倍字符数，英文按 4 字符/token
            if (text == null || text.isEmpty()) return 0;
            int chineseChars = text.length() - text.replaceAll("[\\u4e00-\\u9fff]", "").length();
            int otherChars = text.length() - chineseChars;
            return (int) (chineseChars * 1.5 + otherChars / 4.0);
        }
    }
}
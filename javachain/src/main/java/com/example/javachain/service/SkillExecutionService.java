package com.example.javachain.service;

import com.example.javachain.skill.SkillDefinition;
import com.example.javachain.skill.SkillExecutionResult;
import com.example.javachain.skill.SkillIntentRecognizer;
import com.example.javachain.skill.SkillRegistry;
import com.example.javachain.skill.SkillStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Skill 执行服务。
 *
 * <p>项目中原先只实现了 Skill 的「解析与注册」（{@link SkillRegistry} / {@code SkillParser}）
 * 与「意图识别」（{@link SkillIntentRecognizer}），但没有任何执行入口，导致
 * 前端 {@code /api/skills/*} 系列接口全部缺失。本服务补齐「按名称执行」与
 * 「自动识别并执行」两条链路，执行步骤时复用 {@link McpService} 的工具调用能力
 * （本地 {@code @Tool} 方法与远端 MCP Server 走同一入口）。
 */
@Service
public class SkillExecutionService {

    private static final Logger log = LoggerFactory.getLogger(SkillExecutionService.class);

    /**
     * {@link McpService#executeTool(String, Map)} 内部会吞掉异常并返回可读的错误字符串，
     * 这里按错误前缀识别失败，避免把失败步骤记成成功。
     */
    private static final List<String> FAILURE_PREFIXES = List.of(
            "Tool not found:",
            "Tool execution failed:",
            "解析失败：",
            "错误："
    );

    private final SkillRegistry skillRegistry;
    private final SkillIntentRecognizer intentRecognizer;
    private final McpService mcpService;

    public SkillExecutionService(SkillRegistry skillRegistry,
                                 SkillIntentRecognizer intentRecognizer,
                                 McpService mcpService) {
        this.skillRegistry = skillRegistry;
        this.intentRecognizer = intentRecognizer;
        this.mcpService = mcpService;
    }

    /**
     * 列出全部已注册的 Skill。
     */
    public List<SkillDefinition> listSkills() {
        return skillRegistry.getAll();
    }

    /**
     * 重新扫描并加载 Skill 目录。
     *
     * @return 重新加载后的 Skill 数量
     */
    public int reloadSkills() {
        skillRegistry.loadSkills();
        return skillRegistry.size();
    }

    /**
     * 按名称执行 Skill。
     *
     * @param skillName Skill 名称（SKILL.md 所在目录名）
     * @param params    调用方传入的参数，会覆盖 SKILL.md 中声明的占位参数
     * @return 执行结果，永远不为 null
     */
    public SkillExecutionResult execute(String skillName, Map<String, Object> params) {
        if (skillName == null || skillName.isBlank()) {
            return failed("unknown", "skillName 不能为空，可用 Skill: " + availableNames());
        }

        SkillDefinition skill = skillRegistry.getByName(skillName).orElse(null);
        if (skill == null) {
            return failed(skillName, "Skill 不存在: " + skillName + "，可用 Skill: " + availableNames());
        }

        List<SkillStep> steps = stepsOf(skill);
        if (steps.isEmpty()) {
            return failed(skill.getName(), "Skill " + skill.getName() + " 未从 SKILL.md 中解析出任何执行步骤");
        }

        return run(skill, steps, params);
    }

    /**
     * 自动识别用户意图并执行对应 Skill。
     *
     * <p>识别顺序：
     * <ol>
     *     <li>调用 LLM 做意图识别（{@link SkillIntentRecognizer}）；</li>
     *     <li>未识别出 Skill 时，退化为触发词匹配（{@link SkillRegistry#matchUserInput(String)}）；</li>
     *     <li>仍未命中或命中多个时返回失败结果，并给出可用 Skill 列表。</li>
     * </ol>
     *
     * @param userInput 用户原始输入
     * @param params    调用方显式指定的参数，优先级高于 LLM 抽取的参数
     * @return 执行结果，永远不为 null
     */
    public SkillExecutionResult executeAuto(String userInput, Map<String, Object> params) {
        if (userInput == null || userInput.isBlank()) {
            return failed("unknown", "input 不能为空，可用 Skill: " + availableNames());
        }

        Map<String, Object> merged = new LinkedHashMap<>();
        if (params != null) {
            merged.putAll(params);
        }

        SkillDefinition skill = null;

        SkillIntentRecognizer.IntentResult intent = intentRecognizer.recognize(userInput);
        if (intent != null && intent.getSkillName() != null && !intent.getSkillName().isBlank()) {
            skill = skillRegistry.getByName(intent.getSkillName()).orElse(null);
            if (skill == null) {
                log.warn("意图识别返回了未注册的 Skill: {}", intent.getSkillName());
            } else if (intent.getParams() != null && !intent.getParams().isEmpty()) {
                // LLM 抽取的参数作为基础，调用方显式传入的同名参数覆盖之
                Map<String, Object> fromLlm = new LinkedHashMap<>(intent.getParams());
                fromLlm.putAll(merged);
                merged = fromLlm;
            }
        }

        if (skill == null) {
            List<SkillDefinition> matched = skillRegistry.matchUserInput(userInput);
            if (matched.size() == 1) {
                skill = matched.get(0);
                log.info("意图识别未命中，退化为触发词匹配: {}", skill.getName());
            } else if (matched.size() > 1) {
                return failed("unknown", "匹配到多个 Skill，请显式指定名称: " + namesOf(matched));
            }
        }

        if (skill == null) {
            return failed("unknown", "未能识别出匹配的 Skill，可用 Skill: " + availableNames());
        }

        List<SkillStep> steps = stepsOf(skill);
        if (steps.isEmpty()) {
            return failed(skill.getName(), "Skill " + skill.getName() + " 未从 SKILL.md 中解析出任何执行步骤");
        }

        return run(skill, steps, merged);
    }

    /**
     * 按步骤顺序执行 Skill。
     */
    private SkillExecutionResult run(SkillDefinition skill, List<SkillStep> steps, Map<String, Object> params) {
        SkillExecutionResult result = new SkillExecutionResult(skill.getName());
        Map<String, Object> context = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);

        for (SkillStep step : steps) {
            long startedAt = System.currentTimeMillis();

            SkillExecutionResult.StepResult stepResult = new SkillExecutionResult.StepResult();
            stepResult.setStepOrder(step.getOrder());
            stepResult.setToolName(step.getToolName());

            try {
                Map<String, Object> args = buildArguments(step, context);
                log.info("执行 Skill[{}] 步骤{}({}) 参数: {}", skill.getName(), step.getOrder(), step.getToolName(), args);

                String output = mcpService.executeTool(step.getToolName(), args);
                long cost = System.currentTimeMillis() - startedAt;
                stepResult.setDuration(cost);

                if (isFailureOutput(output)) {
                    stepResult.setSuccess(false);
                    stepResult.setError(output);
                    result.addStepResult(stepResult);
                    result.setErrorMessage("步骤 " + step.getOrder() + "（" + step.getToolName() + "）执行失败: " + output);
                    return result;
                }

                stepResult.setOutput(output);
                stepResult.setSuccess(true);
                result.addStepResult(stepResult);

                if (step.getOutputVariable() != null && !step.getOutputVariable().isBlank()) {
                    result.setContextValue(step.getOutputVariable(), output);
                }
            } catch (Exception e) {
                long cost = System.currentTimeMillis() - startedAt;
                log.warn("执行 Skill[{}] 步骤{} 异常", skill.getName(), step.getOrder(), e);

                stepResult.setSuccess(false);
                stepResult.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                stepResult.setDuration(cost);
                result.addStepResult(stepResult);
                result.setErrorMessage("步骤 " + step.getOrder() + "（" + step.getToolName() + "）异常: " + e.getMessage());
                return result;
            }
        }

        result.setSuccess(true);
        return result;
    }

    /**
     * 构造工具调用参数。
     *
     * <p>SKILL.md 中声明的参数值多为「说明性占位文本」（例如「城市名称（如 "北京"）」），
     * 因此以调用方传入的同名参数为准覆盖之；调用方传入但 SKILL.md 未声明的参数直接透传，
     * 由具体工具自行校验。
     */
    private Map<String, Object> buildArguments(SkillStep step, Map<String, Object> context) {
        Map<String, Object> args = new LinkedHashMap<>();

        if (step.getDefaultParams() != null) {
            args.putAll(step.getDefaultParams());
        }

        for (String key : new ArrayList<>(args.keySet())) {
            Object value = context.get(key);
            if (value != null) {
                args.put(key, value);
            }
        }

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            args.putIfAbsent(entry.getKey(), entry.getValue());
        }

        return args;
    }

    private boolean isFailureOutput(String output) {
        if (output == null || output.isBlank()) {
            return false;
        }
        String trimmed = output.trim();
        for (String prefix : FAILURE_PREFIXES) {
            if (trimmed.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private List<SkillStep> stepsOf(SkillDefinition skill) {
        if (skill.getSteps() == null || skill.getSteps().isEmpty()) {
            return List.of();
        }
        List<SkillStep> steps = new ArrayList<>(skill.getSteps());
        steps.sort(Comparator.comparingInt(SkillStep::getOrder));
        return steps;
    }

    private List<String> namesOf(List<SkillDefinition> skills) {
        List<String> names = new ArrayList<>(skills.size());
        for (SkillDefinition skill : skills) {
            names.add(skill.getName());
        }
        return names;
    }

    private String availableNames() {
        return namesOf(skillRegistry.getAll()).toString();
    }

    private SkillExecutionResult failed(String skillName, String message) {
        SkillExecutionResult result = new SkillExecutionResult(skillName);
        result.setErrorMessage(message);
        return result;
    }
}

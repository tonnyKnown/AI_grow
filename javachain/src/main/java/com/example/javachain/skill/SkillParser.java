package com.example.javachain.skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SkillParser {
    private static final Logger log = LoggerFactory.getLogger(SkillParser.class);

    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    private static final Pattern NAME_PATTERN = Pattern.compile("^name:\\s*[\"']?([^\"']+)[\"']?", Pattern.MULTILINE);
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^description:\\s*[\"']?([^\"']+)[\"']?", Pattern.MULTILINE);

    private static final Pattern STEP_PATTERN = Pattern.compile(
            "^(?:(\\d+)\\.\\s*\\*\\*(.+?)\\*\\*|###\\s*步骤\\s*(\\d+)[:：](.+?))$.*\\n\\s*-\\s*\\*?\\*?使用工具\\*?\\*?:\\s*`([^`]+)`",
            Pattern.MULTILINE);

    private static final Pattern PARAM_PATTERN = Pattern.compile("- `(\\w+)`:\\s*([^\n]+?)(?=\\n(?:\\s*-|\\s*\\w)|$)");

    public SkillDefinition parse(String skillName, String content) {
        try {
            SkillDefinition skill = new SkillDefinition();
            skill.setName(skillName);

            String description = extractDescription(content);
            skill.setDescription(description);
            skill.setTrigger(extractTrigger(description));

            List<SkillStep> steps = extractSteps(content);
            skill.setSteps(steps);

            log.debug("解析 Skill {} 完成，步骤数: {}", skillName, steps.size());
            return skill;

        } catch (Exception e) {
            log.error("解析 Skill {} 失败: {}", skillName, e.getMessage());
            return null;
        }
    }

    private String extractDescription(String content) {
        Matcher descMatcher = DESCRIPTION_PATTERN.matcher(content);
        if (descMatcher.find()) {
            return descMatcher.group(1).trim();
        }
        return "";
    }

    private String extractTrigger(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }
        String[] keywords = {"天气", "查询", "客服", "订单", "物流", "销售", "报告", "审批"};
        for (String keyword : keywords) {
            if (description.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    private List<SkillStep> extractSteps(String content) {
        List<SkillStep> steps = new ArrayList<>();
        Matcher matcher = STEP_PATTERN.matcher(content);

        while (matcher.find()) {
            SkillStep step = new SkillStep();

            String orderStr = matcher.group(1);
            String name1 = matcher.group(2);
            String order2Str = matcher.group(3);
            String name2 = matcher.group(4);
            String toolName = matcher.group(5);

            if (orderStr != null) {
                step.setOrder(Integer.parseInt(orderStr));
                step.setName(name1 != null ? name1.trim() : "");
            } else if (order2Str != null) {
                step.setOrder(Integer.parseInt(order2Str));
                step.setName(name2 != null ? name2.trim() : "");
            }

            step.setToolName(toolName.trim());

            int paramsStart = matcher.end();
            int paramsEnd = findParamsBlockEnd(content, paramsStart);
            String paramsBlock = content.substring(paramsStart, paramsEnd);

            Map<String, Object> params = extractParams(paramsBlock);
            if (!params.isEmpty()) {
                step.setDefaultParams(params);
            }

            steps.add(step);
        }

        return steps;
    }

    private int findParamsBlockEnd(String content, int start) {
        int nextStep = content.indexOf("\n### ", start);
        int nextNum = content.indexOf("\n1. ", start);
        int nextSharp = content.indexOf("\n#", start);
        int nextH2 = content.indexOf("\n## ", start);

        int min = content.length();
        if (nextStep > start) min = Math.min(min, nextStep);
        if (nextNum > start) min = Math.min(min, nextNum);
        if (nextSharp > start) min = Math.min(min, nextSharp);
        if (nextH2 > start) min = Math.min(min, nextH2);

        return min;
    }

    private Map<String, Object> extractParams(String block) {
        Map<String, Object> params = new HashMap<>();
        Matcher matcher = PARAM_PATTERN.matcher(block);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            } else if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }

            params.put(key, value);
        }

        return params;
    }
}

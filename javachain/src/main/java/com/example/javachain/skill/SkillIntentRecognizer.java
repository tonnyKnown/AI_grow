package com.example.javachain.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SkillIntentRecognizer {
    private static final Logger log = LoggerFactory.getLogger(SkillIntentRecognizer.class);

    private final ChatLanguageModel chatLanguageModel;
    private final SkillRegistry skillRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SkillIntentRecognizer(ChatLanguageModel chatLanguageModel, SkillRegistry skillRegistry) {
        this.chatLanguageModel = chatLanguageModel;
        this.skillRegistry = skillRegistry;
    }

    public static class IntentResult {
        private String skillName;
        private Map<String, Object> params;
        private boolean confident;

        public IntentResult() {
            this.params = new HashMap<>();
            this.confident = false;
        }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) { this.params = params; }

        public boolean isConfident() { return confident; }
        public void setConfident(boolean confident) { this.confident = confident; }
    }

    public IntentResult recognize(String userInput) {
        IntentResult result = new IntentResult();

        try {
            List<SkillDefinition> skills = skillRegistry.getAll();
            if (skills.isEmpty()) {
                log.warn("没有可用的 Skills");
                return result;
            }

            String prompt = buildPrompt(userInput, skills);
            log.debug("意图识别提示词:\n{}", prompt);

            String llmResponse = chatLanguageModel.generate(prompt);
            log.debug("LLM 响应:\n{}", llmResponse);

            result = parseLlmResponse(llmResponse);
            log.info("意图识别结果: Skill={}, Params={}, Confident={}",
                    result.skillName, result.params, result.confident);

        } catch (Exception e) {
            log.error("意图识别失败", e);
        }

        return result;
    }

    private String buildPrompt(String userInput, List<SkillDefinition> skills) {
        StringBuilder sb = new StringBuilder();

        sb.append("你是一个意图识别助手，根据用户输入选择最合适的 Skill 并提取参数。\n\n");
        sb.append("可用的 Skills:\n\n");

        for (SkillDefinition skill : skills) {
            sb.append("- name: ").append(skill.getName()).append("\n");
            sb.append("  description: ").append(skill.getDescription()).append("\n");
            sb.append("  trigger: ").append(skill.getTrigger()).append("\n");

            if (!skill.getSteps().isEmpty()) {
                sb.append("  工具参数:\n");
                for (SkillStep step : skill.getSteps()) {
                    if (step.getDefaultParams() != null && !step.getDefaultParams().isEmpty()) {
                        for (Map.Entry<String, Object> entry : step.getDefaultParams().entrySet()) {
                            sb.append("    - ").append(entry.getKey()).append(": 提取").append(entry.getKey()).append("的值\n");
                        }
                    }
                }
            }
            sb.append("\n");
        }

        sb.append("用户输入: ").append(userInput).append("\n\n");
        sb.append("请以 JSON 格式返回结果，格式如下:\n");
        sb.append("{\n");
        sb.append("  \"skillName\": \"选择的 Skill 名称\",\n");
        sb.append("  \"params\": {从用户输入中提取的键值对参数},\n");
        sb.append("  \"confident\": true/false（是否确定识别）\n");
        sb.append("}\n\n");
        sb.append("只返回 JSON，不要其他文字。");

        return sb.toString();
    }

    private IntentResult parseLlmResponse(String response) {
        IntentResult result = new IntentResult();

        try {
            String json = extractJson(response);
            if (json != null) {
                result = objectMapper.readValue(json, IntentResult.class);
            }
        } catch (Exception e) {
            log.warn("解析 LLM 响应失败: {}", response, e);
        }

        return result;
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }
}

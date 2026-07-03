package com.example.javachain.service;

import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 统一的工具服务 - 所有 MCP 工具都在这里定义
 * 使用 @Tool 注解让 McpService 自动发现和注册
 * <p>
 * 优化点：
 * 1. 更完善的工具实现
 * 2. 更好的参数验证
 * 3. 更友好的错误信息
 * 4. 添加更多实用工具
 */
@Service
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Resource
    private RagService ragService;    // ==================== 时间相关工具 ====================

    @Tool("获取当前系统时间，格式：yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        String time = LocalDateTime.now().format(DEFAULT_DATETIME_FORMATTER);
        log.debug("获取当前时间: {}", time);
        return time;
    }


    @Tool("格式化时间戳为可读格式，参数：timestamp(秒)")
    public String formatTimestamp(long timestamp) {
        try {
            String time = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    java.time.ZoneId.systemDefault()
            ).format(DEFAULT_DATETIME_FORMATTER);
            log.debug("格式化时间戳 {} -> {}", timestamp, time);
            return time;
        } catch (Exception e) {
            log.warn("时间戳格式化失败: {}", e.getMessage());
            return "错误：无效的时间戳";
        }
    }

    // ==================== 随机数相关工具 ====================

    @Tool("生成指定范围内的随机数，参数：min(最小值), max(最大值)")
    public String generateRandomNumber(int min, int max) {
        if (min > max) {
            String error = String.format("错误：最小值(%d)不能大于最大值(%d)", min, max);
            log.warn(error);
            return error;
        }

        int result = RANDOM.nextInt(max - min + 1) + min;
        String message = String.format("随机数 [%d, %d]: %d", min, max, result);
        log.debug(message);
        return message;
    }

    @Tool("生成随机 UUID")
    public String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        log.debug("生成 UUID: {}", uuid);
        return uuid;
    }

    @Tool("查询知识库信息,参数：question(问题)")
    public String queryKnowledgeBase(String question) {
        String response = ragService.query(question);
        log.debug("生成 response: {}", response);
        return response;
    }



    // ==================== 辅助方法 ====================


    /**
     * 获取可用技能列表（旧方法，保留向后兼容）
     */
    public String getAvailableSkills() {
        List<Map<String, Object>> skills = new ArrayList<>();

        Map<String, Object> skill1 = new HashMap<>();
        skill1.put("name", "getCurrentTime");
        skill1.put("description", "获取当前系统时间");
        skill1.put("parameters", new ArrayList<>());
        skill1.put("example", "调用 getCurrentTime() 获取当前时间");
        skills.add(skill1);

        Map<String, Object> skill2 = new HashMap<>();
        skill2.put("name", "generateRandomNumber");
        skill2.put("description", "生成指定范围内的随机整数");
        skill2.put("parameters", List.of("min (int)", "max (int)"));
        skill2.put("example", "调用 generateRandomNumber(1, 100)");
        skills.add(skill2);

        Map<String, Object> skill3 = new HashMap<>();
        skill3.put("name", "reverseString");
        skill3.put("description", "反转字符串");
        skill3.put("parameters", List.of("text (String)"));
        skill3.put("example", "调用 reverseString(\"hello\")");
        skills.add(skill3);

        Map<String, Object> skill4 = new HashMap<>();
        skill4.put("name", "addNumbers");
        skill4.put("description", "计算两个数的和");
        skill4.put("parameters", List.of("a (int)", "b (int)"));
        skill4.put("example", "调用 addNumbers(1, 2)");
        skills.add(skill4);

        return toJson(skills);
    }


    private String toJson(List<Map<String, Object>> skills) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < skills.size(); i++) {
            Map<String, Object> skill = skills.get(i);
            json.append("  {\n");
            json.append("    \"name\": \"").append(skill.get("name")).append("\",\n");
            json.append("    \"description\": \"").append(skill.get("description")).append("\",\n");

            @SuppressWarnings("unchecked")
            List<String> params = (List<String>) skill.get("parameters");
            json.append("    \"parameters\": [");
            for (int j = 0; j < params.size(); j++) {
                json.append("\"").append(params.get(j)).append("\"");
                if (j < params.size() - 1) json.append(", ");
            }
            json.append("],\n");

            json.append("    \"example\": \"").append(skill.get("example")).append("\"\n");
            json.append("  }");
            if (i < skills.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        return json.toString();
    }
}

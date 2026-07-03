package com.example.javachain.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 工具参数验证服务
 *
 * 提供 JSON Schema 风格的参数验证
 */
@Slf4j
@Service
public class ToolParameterValidator {

    private final ObjectMapper objectMapper;

    /** 工具参数 Schema 缓存 */
    private final Map<String, ToolSchema> schemaCache = new ConcurrentHashMap<>();

    public ToolParameterValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initBuiltinSchemas();
    }

    /**
     * 初始化内置的 Schema
     */
    private void initBuiltinSchemas() {
        // 天气查询 Schema
        schemaCache.put("weather_forecast", ToolSchema.builder()
                .name("weather_forecast")
                .required(List.of("city"))
                .properties(Map.of(
                        "city", ToolSchema.Property.builder()
                                .type("string")
                                .description("城市名称")
                                .minLength(1)
                                .maxLength(50)
                                .pattern("^[\\u4e00-\\u9fa5a-zA-Z]+$")
                                .build(),
                        "days", ToolSchema.Property.builder()
                                .type("integer")
                                .description("查询天数")
                                .minimum(1.0)
                                .maximum(7.0)
                                .defaultValue(1)
                                .build()
                ))
                .build());

        // 文件删除 Schema
        schemaCache.put("filesystem_delete", ToolSchema.builder()
                .name("filesystem_delete")
                .required(List.of("path"))
                .properties(Map.of(
                        "path", ToolSchema.Property.builder()
                                .type("string")
                                .description("文件路径")
                                .minLength(1)
                                .maxLength(500)
                                .pattern("^[a-zA-Z0-9/_\\\\-\\.]+$")
                                .build()
                ))
                .dangerous(true)
                .build());

        // 文件写入 Schema
        schemaCache.put("filesystem_write", ToolSchema.builder()
                .name("filesystem_write")
                .required(List.of("path"))
                .properties(Map.of(
                        "path", ToolSchema.Property.builder()
                                .type("string")
                                .description("文件路径")
                                .minLength(1)
                                .maxLength(500)
                                .pattern("^[a-zA-Z0-9/_\\\\-\\.]+$")
                                .build(),
                        "content", ToolSchema.Property.builder()
                                .type("string")
                                .description("文件内容")
                                .maxLength(1048576) // 最大 1MB
                                .build()
                ))
                .dangerous(true)
                .build());

        // SQL 查询 Schema
        schemaCache.put("mysql_query", ToolSchema.builder()
                .name("mysql_query")
                .required(List.of("sql"))
                .properties(Map.of(
                        "sql", ToolSchema.Property.builder()
                                .type("string")
                                .description("SQL 查询语句")
                                .maxLength(1000)
                                .build()
                ))
                .dangerous(true)
                .build());
    }

    /**
     * 验证工具参数
     */
    public ValidationResult validate(String toolName, Map<String, Object> arguments) {
        ToolSchema schema = schemaCache.get(toolName);

        if (schema == null) {
            // 没有 Schema，允许执行
            return ValidationResult.success();
        }

        List<String> errors = new ArrayList<>();

        // 检查必需参数
        for (String required : schema.getRequired()) {
            if (!arguments.containsKey(required) || arguments.get(required) == null) {
                errors.add("缺少必需参数: " + required);
            }
        }

        // 验证每个参数
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            ToolSchema.Property prop = schema.getProperties().get(key);
            if (prop != null && value != null) {
                // 类型检查
                if (!validateType(key, value, prop, errors)) {
                    // 已在 validateType 中添加错误
                }

                // 字符串长度检查
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (prop.getMinLength() != null && strValue.length() < prop.getMinLength()) {
                        errors.add(String.format("参数 %s 长度不能小于 %d", key, prop.getMinLength()));
                    }
                    if (prop.getMaxLength() != null && strValue.length() > prop.getMaxLength()) {
                        errors.add(String.format("参数 %s 长度不能大于 %d", key, prop.getMaxLength()));
                    }
                    if (prop.getPattern() != null && !Pattern.matches(prop.getPattern(), strValue)) {
                        errors.add(String.format("参数 %s 格式不正确，应匹配: %s", key, prop.getPattern()));
                    }
                }

                // 数字范围检查
                if (value instanceof Number) {
                    double numValue = ((Number) value).doubleValue();
                    if (prop.getMinimum() != null && numValue < prop.getMinimum()) {
                        errors.add(String.format("参数 %s 不能小于 %f", key, prop.getMinimum()));
                    }
                    if (prop.getMaximum() != null && numValue > prop.getMaximum()) {
                        errors.add(String.format("参数 %s 不能大于 %f", key, prop.getMaximum()));
                    }
                }
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(String.join("; ", errors));
        }
    }

    /**
     * 类型验证
     */
    private boolean validateType(String key, Object value, ToolSchema.Property prop, List<String> errors) {
        String expectedType = prop.getType();
        boolean valid = true;

        switch (expectedType) {
            case "string":
                if (!(value instanceof String)) {
                    errors.add(String.format("参数 %s 必须是字符串类型", key));
                    valid = false;
                }
                break;
            case "integer":
            case "number":
                if (!(value instanceof Number)) {
                    // 尝试转换
                    try {
                        Double.parseDouble(value.toString());
                    } catch (NumberFormatException e) {
                        errors.add(String.format("参数 %s 必须是数字类型", key));
                        valid = false;
                    }
                }
                break;
            case "boolean":
                if (!(value instanceof Boolean)) {
                    if (!("true".equalsIgnoreCase(value.toString()) || "false".equalsIgnoreCase(value.toString()))) {
                        errors.add(String.format("参数 %s 必须是布尔类型", key));
                        valid = false;
                    }
                }
                break;
            case "array":
                if (!(value instanceof List)) {
                    errors.add(String.format("参数 %s 必须是数组类型", key));
                    valid = false;
                }
                break;
            case "object":
                if (!(value instanceof Map)) {
                    errors.add(String.format("参数 %s 必须是对象类型", key));
                    valid = false;
                }
                break;
        }
        return valid;
    }

    /**
     * 注册自定义 Schema
     */
    public void registerSchema(ToolSchema schema) {
        schemaCache.put(schema.getName(), schema);
        log.info("注册工具参数 Schema: {}", schema.getName());
    }

    /**
     * 获取 Schema
     */
    public ToolSchema getSchema(String toolName) {
        return schemaCache.get(toolName);
    }

    /**
     * 检查是否是危险工具
     */
    public boolean isDangerousTool(String toolName) {
        ToolSchema schema = schemaCache.get(toolName);
        return schema != null && schema.isDangerous();
    }

    /**
     * 验证结果
     */
    @lombok.Data
    @lombok.Builder
    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;
        private List<String> errors;

        public static ValidationResult success() {
            return ValidationResult.builder()
                    .valid(true)
                    .build();
        }

        public static ValidationResult failure(String errorMessage) {
            return ValidationResult.builder()
                    .valid(false)
                    .errorMessage(errorMessage)
                    .errors(List.of(errorMessage))
                    .build();
        }

        public static ValidationResult failure(List<String> errors) {
            return ValidationResult.builder()
                    .valid(false)
                    .errorMessage(String.join("; ", errors))
                    .errors(errors)
                    .build();
        }
    }

    /**
     * 工具 Schema
     */
    @lombok.Data
    @lombok.Builder
    public static class ToolSchema {
        private String name;
        private String description;
        private List<String> required;
        private Map<String, Property> properties;
        private boolean dangerous;

        @lombok.Data
        @lombok.Builder
        public static class Property {
            private String type;
            private String description;
            private Integer minLength;
            private Integer maxLength;
            private Double minimum;
            private Double maximum;
            private String pattern;
            private Object defaultValue;
        }
    }
}

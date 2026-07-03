package com.example.javachain.skill;

import java.util.Map;

public class SkillStep {
    private int order;
    private String name;
    private String description;
    private String toolName;
    private Map<String, Object> defaultParams;
    private String condition;
    private String outputVariable;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getDefaultParams() {
        return defaultParams;
    }

    public void setDefaultParams(Map<String, Object> defaultParams) {
        this.defaultParams = defaultParams;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOutputVariable() {
        return outputVariable;
    }

    public void setOutputVariable(String outputVariable) {
        this.outputVariable = outputVariable;
    }
}

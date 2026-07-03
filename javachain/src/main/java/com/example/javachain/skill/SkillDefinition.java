package com.example.javachain.skill;

import java.util.List;

public class SkillDefinition {
    private String name;
    private String description;
    private String trigger;
    private List<SkillStep> steps;
    private String inputFormat;
    private String outputFormat;

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

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public List<SkillStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SkillStep> steps) {
        this.steps = steps;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public boolean matches(String userInput) {
        if (trigger == null || trigger.isEmpty()) {
            return false;
        }
        String lowerInput = userInput.toLowerCase();
        String lowerTrigger = trigger.toLowerCase();
        return lowerInput.contains(lowerTrigger) || lowerTrigger.contains(lowerInput);
    }
}

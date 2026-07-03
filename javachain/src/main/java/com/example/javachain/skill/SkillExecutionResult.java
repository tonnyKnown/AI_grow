package com.example.javachain.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillExecutionResult {
    private String skillName;
    private boolean success;
    private String errorMessage;
    private long startTime;
    private long endTime;
    private List<StepResult> stepResults;
    private Map<String, Object> context;

    public SkillExecutionResult(String skillName) {
        this.skillName = skillName;
        this.startTime = System.currentTimeMillis();
        this.stepResults = new ArrayList<>();
        this.context = new HashMap<>();
    }

    public void addStepResult(StepResult stepResult) {
        this.stepResults.add(stepResult);
    }

    public void setSuccess(boolean success) {
        this.success = success;
        this.endTime = System.currentTimeMillis();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        this.endTime = System.currentTimeMillis();
    }

    public String getSkillName() {
        return skillName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public List<StepResult> getStepResults() {
        return stepResults;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContextValue(String key, Object value) {
        this.context.put(key, value);
    }

    public Object getContextValue(String key) {
        return this.context.get(key);
    }

    public String formatReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("┌─────────────────────────────────────┐\n");
        sb.append("│  Skill 执行报告: ").append(skillName).append("\n");
        sb.append("├─────────────────────────────────────┤\n");
        sb.append("│  状态: ").append(success ? "✅ 成功" : "❌ 失败").append("\n");
        sb.append("│  耗时: ").append(getDuration()).append("ms\n");
        sb.append("├─────────────────────────────────────┤\n");

        for (StepResult sr : stepResults) {
            sb.append("│  步骤 ").append(sr.getStepOrder()).append(": ").append(sr.getToolName()).append("\n");
            sb.append("│    ").append(sr.isSuccess() ? "✅" : "❌").append(" ");
            if (sr.isSuccess()) {
                String output = sr.getOutput();
                if (output.length() > 50) {
                    output = output.substring(0, 50) + "...";
                }
                sb.append(output.replace("\n", " ")).append("\n");
            } else {
                sb.append(sr.getError()).append("\n");
            }
        }

        sb.append("└─────────────────────────────────────┘\n");
        return sb.toString();
    }

    public static class StepResult {
        private int stepOrder;
        private String toolName;
        private String output;
        private boolean success;
        private String error;
        private long duration;

        public int getStepOrder() {
            return stepOrder;
        }

        public void setStepOrder(int stepOrder) {
            this.stepOrder = stepOrder;
        }

        public String getToolName() {
            return toolName;
        }

        public void setToolName(String toolName) {
            this.toolName = toolName;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}

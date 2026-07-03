package com.example.javachain.agent;

import lombok.Data;
import lombok.Builder;

/**
 * ReAct 推理步骤
 * 
 * ReAct (Reasoning + Acting) 模式的核心数据结构
 * - Thought: 思考当前状态和分析
 * - Action: 选择要执行的工具
 * - ActionInput: 工具输入参数
 * - Observation: 执行结果
 */
@Data
@Builder
public class ReasoningStep {
    
    /**
     * 思考过程：分析当前情况
     */
    private String thought;
    
    /**
     * 选择要执行的工具名
     */
    private String action;
    
    /**
     * 工具输入参数（JSON 格式）
     */
    private String actionInput;
    
    /**
     * 工具执行结果
     */
    private String observation;
    
    /**
     * 步骤编号
     */
    private int stepNumber;
    
    /**
     * 是否为最终答案
     */
    private boolean isFinalAnswer;
    
    /**
     * 最终答案内容
     */
    private String answerContent;
    
    /**
     * 步骤执行状态
     */
    private StepStatus status;
    
    /**
     * 错误信息（如有）
     */
    private String errorMessage;
    
    /**
     * 是否等待用户确认
     */
    private boolean pendingConfirmation;
    
    /**
     * 待确认操作描述
     */
    private String pendingDescription;

    public boolean hasToolCall() {
        return action != null && actionInput != null && !action.trim().isEmpty() && !actionInput.trim().isEmpty();
    }
    
    /**
     * 判断是否有待确认操作
     */
    public boolean isPendingConfirmation() {
        return pendingConfirmation;
    }

    public enum StepStatus {
        /** 思考中 */
        THINKING,
        /** 执行中 */
        EXECUTING,
        /** 观察结果 */
        OBSERVING,
        /** 完成 */
        COMPLETED,
        /** 失败 */
        FAILED,
        /** 最终答案 */
        FINAL,
        /** 等待用户确认 */
        PENDING_CONFIRMATION
    }
    
    /**
     * 转换为可读格式
     */
    @Override
    public String toString() {
        if (isFinalAnswer) {
            return String.format("【步骤 %d - 最终答案】\n%s", stepNumber, answerContent);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【步骤 %d】\n", stepNumber));
        
        if (thought != null) {
            sb.append(String.format("💭 思考: %s\n", thought));
        }
        
        if (action != null) {
            sb.append(String.format("🔧 行动: %s\n", action));
        }
        
        if (actionInput != null) {
            sb.append(String.format("📥 输入: %s\n", actionInput));
        }
        
        if (observation != null) {
            sb.append(String.format("📤 观察: %s\n", observation));
        }
        
        if (errorMessage != null) {
            sb.append(String.format("❌ 错误: %s\n", errorMessage));
        }
        
        return sb.toString();
    }
}

package com.example.javachain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct Agent 配置类
 * 
 * 集中管理 Agent 的所有配置参数，支持通过配置文件动态调整
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "javachain.agent")
public class ReActAgentConfig {

    /**
     * 最大推理步骤数
     */
    private int maxSteps = 15;

    /**
     * 单一工具最大调用次数
     */
    private int maxToolCallsPerTool = 3;

    /**
     * 超时时间（毫秒）
     */
    private long timeoutMs = 60000;

    /**
     * 连续失败最大次数
     */
    private int maxConsecutiveFailures = 2;

    /**
     * 工具调用重试次数
     */
    private int retryCount = 2;

    /**
     * 重试间隔（毫秒）
     */
    private long retryDelayMs = 1000;

    /**
     * 工具列表缓存过期时间（毫秒）
     */
    private long toolCacheExpireMs = 300000;

    /**
     * 敏感关键词列表
     */
    private List<String> sensitiveKeywords = new ArrayList<>(List.of(
            "删除", "删除所有", "drop", "truncate", "rm -rf",
            "格式化", "format", "shutdown", "重启系统"
    ));

    /**
     * 需要用户确认的危险工具列表
     */
    private List<String> dangerousTools = new ArrayList<>(List.of(
            "filesystem_delete",
            "filesystem_write"
    ));

    /**
     * 最大 Prompt Token 数（防止超出 LLM context window）
     * 建议设置为模型 context 的 80%
     */
    private int maxPromptTokens = 8000;

    /**
     * 历史摘要开关
     */
    private boolean enableHistorySummarization = true;

    /**
     * 历史摘要触发阈值（当 token 数超过 maxPromptTokens 的此比例时触发）
     */
    private double summarizationThreshold = 0.8;
}
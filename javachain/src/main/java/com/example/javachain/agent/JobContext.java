package com.example.javachain.agent;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class JobContext {
    private final String jobId;
    private final String question;
    private volatile JobStatus status;
    private volatile String pendingToolName;
    private volatile Map<String, Object> pendingToolArgs;
    private volatile String pendingDescription;
    private volatile String lastSteps;
    private volatile String finalAnswer;
    private volatile String errorMessage;
    private volatile long startTime;
    private volatile long duration;
    private volatile List<Map<String, Object>> reasoningHistory;

    public enum JobStatus {
        RUNNING,
        PENDING_CONFIRMATION,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    private static final Map<String, JobContext> JOBS = new ConcurrentHashMap<>();

    public JobContext(String jobId, String question) {
        this.jobId = jobId;
        this.question = question;
        this.status = JobStatus.RUNNING;
        this.startTime = System.currentTimeMillis();
        this.reasoningHistory = new java.util.ArrayList<>();
        JOBS.put(jobId, this);
    }

    public static JobContext get(String jobId) {
        return JOBS.get(jobId);
    }

    public static void remove(String jobId) {
        JOBS.remove(jobId);
    }

    public void markPendingConfirmation(String toolName, Map<String, Object> toolArgs, String description) {
        this.pendingToolName = toolName;
        this.pendingToolArgs = toolArgs;
        this.pendingDescription = description;
        this.status = JobStatus.PENDING_CONFIRMATION;
    }

    public void addReasoningStep(String step) {
        this.lastSteps = step;
        this.reasoningHistory.add(Map.of("step", step, "timestamp", System.currentTimeMillis()));
    }

    public void complete(String answer, long duration) {
        this.finalAnswer = answer;
        this.duration = duration;
        this.status = JobStatus.COMPLETED;
    }

    public void cancel() {
        this.status = JobStatus.CANCELLED;
        this.finalAnswer = "用户已取消操作";
    }

    public void fail(String error) {
        this.errorMessage = error;
        this.status = JobStatus.FAILED;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > 300_000;
    }
}
package com.example.javachain.controller;

import com.example.javachain.agent.JobContext;
import com.example.javachain.agent.ReActAgent;
import com.example.javachain.common.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 推理 Agent 控制器
 * 
 * 提供自主推理和多步工具调用的 API
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ReActAgent reActAgent;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AgentController(ReActAgent reActAgent, ObjectMapper objectMapper) {
        this.reActAgent = reActAgent;
        this.objectMapper = objectMapper;
    }

    /**
     * 启动推理任务
     * 
     * POST /api/agent/react/start
     * Body: {"question": "删除 temp/test.txt"}
     */
    @PostMapping("/react/start")
    public ApiResult<Map<String, Object>> startReact(@RequestBody QuestionRequest request) {
        log.info("📥 启动推理任务: {}", request.getQuestion());
        
        String jobId = UUID.randomUUID().toString();
        JobContext job = new JobContext(jobId, request.getQuestion());
        
        executor.execute(() -> {
            ReActAgent.ReasoningResult result = reActAgent.executeJob(job);
            job.setFinalAnswer(result.isSuccess() ? result.getAnswer() : result.getErrorMessage());
            job.setDuration(result.getDuration());
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", jobId);
        data.put("status", "started");
        
        return ApiResult.success(data);
    }
    
    /**
     * 查询任务状态
     * 
     * GET /api/agent/react/status/{jobId}
     */
    @GetMapping("/react/status/{jobId}")
    public ApiResult<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        JobContext job = JobContext.get(jobId);
        
        if (job == null) {
            return ApiResult.error(404, "任务不存在", null);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", job.getJobId());
        data.put("status", job.getStatus().name().toLowerCase());
        data.put("question", job.getQuestion());
        
        if (job.getStatus() == JobContext.JobStatus.PENDING_CONFIRMATION) {
            data.put("toolName", job.getPendingToolName());
            data.put("description", job.getPendingDescription());
            data.put("actionInput", job.getPendingToolArgs());
        }
        
        if (job.getStatus() == JobContext.JobStatus.COMPLETED || 
            job.getStatus() == JobContext.JobStatus.CANCELLED ||
            job.getStatus() == JobContext.JobStatus.FAILED) {
            data.put("answer", job.getFinalAnswer());
            data.put("duration", job.getDuration());
            data.put("history", job.getReasoningHistory());
        }
        
        return ApiResult.success(data);
    }
    
    /**
     * 确认或取消操作
     * 
     * POST /api/agent/react/confirm
     * Body: {"jobId": "xxx", "confirm": true}
     */
    @PostMapping("/react/confirm")
    public ApiResult<Map<String, Object>> confirmJob(@RequestBody ConfirmRequest request) {
        log.info("📥 收到确认请求: jobId={}, confirm={}", request.getJobId(), request.isConfirm());
        
        JobContext job = JobContext.get(request.getJobId());
        
        if (job == null) {
            return ApiResult.error(404, "任务不存在", null);
        }
        
        if (job.getStatus() != JobContext.JobStatus.PENDING_CONFIRMATION) {
            return ApiResult.error(400, "任务不在待确认状态", null);
        }
        
        if (!request.isConfirm()) {
            job.cancel();
            Map<String, Object> data = new HashMap<>();
            data.put("jobId", job.getJobId());
            data.put("status", "cancelled");
            data.put("message", "用户已取消操作");
            return ApiResult.success(data);
        }
        
        executor.execute(() -> {
            ReActAgent.ReasoningResult result = reActAgent.continueJob(job, true);
            job.setFinalAnswer(result.isSuccess() ? result.getAnswer() : result.getErrorMessage());
            job.setDuration(result.getDuration());
        });
        
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", job.getJobId());
        data.put("status", "confirmed");
        
        return ApiResult.success(data);
    }

    /**
     * 执行自主推理任务（简化版 - 内部使用）
     */
    @PostMapping("/react")
    public ApiResult<Map<String, Object>> react(@RequestBody ReactRequest request) {
        log.info("📥 收到推理请求: {}", request.getQuestion());
        
        String jobId = UUID.randomUUID().toString();
        JobContext job = new JobContext(jobId, request.getQuestion());
        
        ReActAgent.ReasoningResult result = reActAgent.executeJob(job);
        
        Map<String, Object> data = new HashMap<>();
        data.put("question", result.getQuestion());
        
        if (result.isPendingConfirmation()) {
            data.put("status", "pending_confirmation");
            data.put("jobId", jobId);
            data.put("toolName", result.getPendingTool());
            data.put("description", result.getPendingDescription());
            data.put("actionInput", result.getPendingActionInput());
            data.put("steps", result.getSteps().size());
            return ApiResult.success(data);
        }
        
        data.put("answer", result.isSuccess() ? result.getAnswer() : result.getErrorMessage());
        data.put("duration", result.getDuration());
        data.put("steps", result.getSteps().size());
        data.put("status", result.isSuccess() ? "completed" : "failed");
        
        return result.isSuccess()
            ? ApiResult.success(data)
            : ApiResult.error(result.getErrorMessage(), data);
    }

    /**
     * 快速推理（简化输出）
     */
    @PostMapping("/react/quick")
    public ApiResult<Map<String, Object>> reactQuick(@RequestBody QuestionRequest request) {
        log.info("⚡ 收到快速推理请求: {}", request.getQuestion());
        
        ReActAgent.ReasoningResult result = reActAgent.execute(request.getQuestion());
        
        Map<String, Object> data = new HashMap<>();
        data.put("answer", result.isSuccess() ? result.getAnswer() : result.getErrorMessage());
        data.put("steps", result.getSteps().size());
        data.put("duration", result.getDuration());
        
        return result.isSuccess()
            ? ApiResult.success(data)
            : ApiResult.error(result.getErrorMessage(), data);
    }

    /**
     * 获取推理过程详情
     */
    @GetMapping("/steps")
    public ApiResult<Map<String, Object>> getStepInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("name", "ReAct 推理引擎");
        info.put("description", "支持自主推理和多步工具调用的智能 Agent");
        
        Map<String, String> workflow = new HashMap<>();
        workflow.put("1_thought", "思考 - 分析当前状态和目标");
        workflow.put("2_action", "行动 - 选择合适的工具");
        workflow.put("3_input", "输入 - 准备工具参数");
        workflow.put("4_observation", "观察 - 获取执行结果");
        workflow.put("5_repeat", "重复 - 直到任务完成");
        info.put("workflow", workflow);
        
        Map<String, Object> safety = new HashMap<>();
        safety.put("maxSteps", 10);
        safety.put("maxCallsPerTool", 5);
        safety.put("timeout", "30秒");
        safety.put("sensitiveCheck", "启用");
        safety.put("dangerousOperations", "需要用户确认");
        info.put("safety", safety);
        
        return ApiResult.success(info);
    }

    /**
     * 流式推理接口（SSE）
     */
    @PostMapping("/react/stream")
    public SseEmitter reactStream(@RequestBody QuestionRequest request) {
        log.info("🌊 收到流式推理请求: {}", request.getQuestion());
        
        SseEmitter emitter = new SseEmitter(60000L);
        
        executor.execute(() -> {
            try {
                ReActAgent.ReasoningResult result = reActAgent.executeWithCallback(
                    request.getQuestion(),
                    (step, total) -> {
                        try {
                            Map<String, Object> stepData = new HashMap<>();
                            stepData.put("stepNumber", step.getStepNumber());
                            stepData.put("thought", step.getThought());
                            stepData.put("action", step.getAction());
                            stepData.put("actionInput", step.getActionInput());
                            stepData.put("observation", step.getObservation());
                            stepData.put("isFinal", step.isFinalAnswer());
                            stepData.put("totalSteps", total);
                            
                            emitter.send(SseEmitter.event()
                                .name("step")
                                .data(objectMapper.writeValueAsString(stepData)));
                        } catch (IOException e) {
                            log.error("发送步骤事件失败", e);
                        }
                    }
                );
                
                Map<String, Object> finalData = new HashMap<>();
                finalData.put("success", result.isSuccess());
                finalData.put("answer", result.getAnswer());
                finalData.put("errorMessage", result.getErrorMessage());
                finalData.put("duration", result.getDuration());
                finalData.put("totalSteps", result.getSteps().size());
                
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(objectMapper.writeValueAsString(finalData)));
                
                emitter.complete();
            } catch (Exception e) {
                log.error("流式推理执行失败", e);
                try {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("error", e.getMessage());
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(objectMapper.writeValueAsString(errorData)));
                    emitter.completeWithError(e);
                } catch (IOException ioException) {
                    log.error("发送错误事件失败", ioException);
                }
            }
        });
        
        emitter.onCompletion(() -> log.debug("SSE 连接关闭"));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        
        return emitter;
    }

    @Data
    public static class QuestionRequest {
        private String question;
    }
    
    @Data
    public static class ReactRequest {
        private String question;
    }
    
    @Data
    public static class ConfirmRequest {
        private String jobId;
        private boolean confirm;
    }
}

package com.example.javachain.controller;

import com.example.javachain.common.ApiResult;
import com.example.javachain.service.SkillExecutionService;
import com.example.javachain.skill.SkillDefinition;
import com.example.javachain.skill.SkillExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Skill 控制器。
 *
 * <p>对外提供 Skill 的查询、重新加载与执行能力，网关侧路径为
 * {@code /api/javachain/skills/**}（经 RewritePath 映射到 {@code /api/skills/**}）。
 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private static final Logger log = LoggerFactory.getLogger(SkillController.class);

    private final SkillExecutionService skillExecutionService;

    public SkillController(SkillExecutionService skillExecutionService) {
        this.skillExecutionService = skillExecutionService;
    }

    /**
     * 列出全部已注册的 Skill。
     */
    @GetMapping("/list")
    public ApiResult<List<SkillDefinition>> list() {
        return ApiResult.success(skillExecutionService.listSkills());
    }

    /**
     * 重新扫描并加载 Skill 目录。
     */
    @GetMapping("/reload")
    public ApiResult<String> reload() {
        int count = skillExecutionService.reloadSkills();
        log.info("Skills 重新加载完成，共 {} 个", count);
        return ApiResult.success("Skills 已重新加载，共 " + count + " 个");
    }

    /**
     * 按名称执行 Skill。
     *
     * @param skillId Skill 名称
     * @param params  执行参数，可为空
     */
    @PostMapping("/execute/{skillId}")
    public ApiResult<SkillExecutionResult> execute(@PathVariable String skillId,
                                                   @RequestBody(required = false) Map<String, Object> params) {
        log.info("执行 Skill: {}，参数: {}", skillId, params);
        SkillExecutionResult result = skillExecutionService.execute(skillId, params);
        return toApiResult(result);
    }

    /**
     * 自动识别用户意图并执行 Skill。
     *
     * <p>请求体：{@code {"input": "北京今天天气怎么样", "params": {}}}
     */
    @PostMapping("/auto")
    public ApiResult<SkillExecutionResult> autoExecute(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = request == null ? Map.of() : request;

        String input = asString(body.get("input"));
        Map<String, Object> params = asMap(body.get("params"));

        log.info("自动执行 Skill，输入: {}，参数: {}", input, params);
        SkillExecutionResult result = skillExecutionService.executeAuto(input, params);
        return toApiResult(result);
    }

    private ApiResult<SkillExecutionResult> toApiResult(SkillExecutionResult result) {
        if (result.isSuccess()) {
            return ApiResult.success(result);
        }
        // 失败时同时返回结果明细，便于前端展示每一步的执行情况
        return ApiResult.error(result.getErrorMessage() == null ? "Skill 执行失败" : result.getErrorMessage(), result);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
            }
            return result;
        }
        return new LinkedHashMap<>();
    }
}

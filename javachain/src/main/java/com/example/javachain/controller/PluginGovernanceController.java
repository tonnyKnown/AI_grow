package com.example.javachain.controller;

import com.example.javachain.common.ApiResult;
import com.example.javachain.plugin.PluginMetadata;
import com.example.javachain.service.PluginGovernanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 插件治理控制器。
 *
 * <p>提供插件的列表、启用 / 禁用、兼容性判定与版本比较能力。
 * 网关侧路径为 {@code /api/javachain/plugin-governance/**}。
 */
@RestController
@RequestMapping("/api/plugin-governance")
public class PluginGovernanceController {

    private static final Logger log = LoggerFactory.getLogger(PluginGovernanceController.class);

    private final PluginGovernanceService pluginGovernanceService;

    public PluginGovernanceController(PluginGovernanceService pluginGovernanceService) {
        this.pluginGovernanceService = pluginGovernanceService;
    }

    /**
     * 列出已登记的插件。
     *
     * @param includeDisabled 是否包含已禁用的插件，默认 false
     */
    @GetMapping("/list")
    public ApiResult<List<PluginMetadata>> list(
            @RequestParam(value = "includeDisabled", defaultValue = "false") boolean includeDisabled) {
        return ApiResult.success(pluginGovernanceService.list(includeDisabled));
    }

    /**
     * 启用插件。
     */
    @PostMapping("/enable/{pluginId}")
    public ApiResult<PluginMetadata> enable(@PathVariable String pluginId) {
        try {
            PluginMetadata plugin = pluginGovernanceService.enable(pluginId);
            return ApiResult.success("插件已启用: " + plugin.getName(), plugin);
        } catch (IllegalArgumentException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 禁用插件。
     */
    @PostMapping("/disable/{pluginId}")
    public ApiResult<PluginMetadata> disable(@PathVariable String pluginId) {
        try {
            PluginMetadata plugin = pluginGovernanceService.disable(pluginId);
            return ApiResult.success("插件已禁用: " + plugin.getName(), plugin);
        } catch (IllegalArgumentException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 判定插件是否满足目标版本。
     */
    @GetMapping("/check/{pluginId}/{version}")
    public ApiResult<Map<String, Object>> checkCompatibility(@PathVariable String pluginId,
                                                             @PathVariable String version) {
        try {
            return ApiResult.success(pluginGovernanceService.checkCompatibility(pluginId, version));
        } catch (IllegalArgumentException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 比较两个版本号。
     *
     * <p>请求体：{@code {"v1": "1.2.0", "v2": "1.10.0"}}
     */
    @PostMapping("/compare")
    public ApiResult<Map<String, Object>> compareVersions(@RequestBody(required = false) Map<String, String> request) {
        Map<String, String> body = request == null ? Map.of() : request;

        String v1 = body.get("v1");
        String v2 = body.get("v2");
        if (v1 == null || v1.isBlank() || v2 == null || v2.isBlank()) {
            return ApiResult.error("v1 与 v2 不能为空");
        }

        log.info("比较版本: {} vs {}", v1, v2);
        return ApiResult.success(pluginGovernanceService.compareVersions(v1, v2));
    }
}

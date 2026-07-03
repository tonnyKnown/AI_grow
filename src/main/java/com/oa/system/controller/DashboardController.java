package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.mapper.DashboardMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    @Resource
    private DashboardMapper dashboardMapper;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardMapper.getDashboardStats());
    }
}

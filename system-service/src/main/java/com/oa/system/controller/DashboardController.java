package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.entity.DashboardStats;
import com.oa.system.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        return Result.success(dashboardService.getStats());
    }
}

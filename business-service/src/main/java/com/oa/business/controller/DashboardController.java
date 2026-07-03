package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.entity.DashboardStats;
import com.oa.business.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        return Result.success(dashboardService.getStats());
    }
}

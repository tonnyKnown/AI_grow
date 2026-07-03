package com.oa.business.feign;

import com.oa.business.common.Result;
import com.oa.business.entity.DashboardStats;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "system-service", url = "${feign.system-service.url:http://localhost:8081}")
public interface SystemServiceFeign {

    @GetMapping("/system/dashboard/stats")
    Result<DashboardStats> getStats();
}

package com.oa.system.service.impl;

import com.oa.system.entity.DashboardStats;
import com.oa.system.mapper.UserMapper;
import com.oa.system.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalUsers(userMapper.count());
        stats.setTotalProducts(0L);
        stats.setTotalOrders(0L);
        stats.setTotalMarketing(0L);
        return stats;
    }
}

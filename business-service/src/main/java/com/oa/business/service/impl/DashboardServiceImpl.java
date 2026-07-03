package com.oa.business.service.impl;

import com.oa.business.common.Result;
import com.oa.business.entity.DashboardStats;
import com.oa.business.feign.SystemServiceFeign;
import com.oa.business.mapper.ProductMapper;
import com.oa.business.mapper.OrderMapper;
import com.oa.business.mapper.MarketingMapper;
import com.oa.business.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private SystemServiceFeign systemServiceFeign;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MarketingMapper marketingMapper;

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        try {
            Result<DashboardStats> systemStatsResult = systemServiceFeign.getStats();
            if (systemStatsResult != null && systemStatsResult.getCode() == 200 && systemStatsResult.getData() != null) {
                stats.setTotalUsers(systemStatsResult.getData().getTotalUsers());
            } else {
                stats.setTotalUsers(0L);
            }
        } catch (Exception e) {
            stats.setTotalUsers(0L);
        }

        stats.setTotalProducts(productMapper.count());
        stats.setTotalOrders(orderMapper.count());
        stats.setTotalMarketing(marketingMapper.count());
        return stats;
    }
}

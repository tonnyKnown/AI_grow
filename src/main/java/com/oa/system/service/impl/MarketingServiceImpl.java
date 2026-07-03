package com.oa.system.service.impl;

import com.oa.system.entity.Marketing;
import com.oa.system.mapper.MarketingMapper;
import com.oa.system.service.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 营销活动服务实现类
 */
@Service
public class MarketingServiceImpl implements MarketingService {

    @Autowired
    private MarketingMapper marketingMapper;

    @Override
    public List<Marketing> getAllMarketing() {
        return marketingMapper.selectAll();
    }

    @Override
    public Marketing getMarketingById(Long id) {
        return marketingMapper.selectById(id);
    }

    @Override
    public List<Marketing> getMarketingPage(String name, String type, Integer status, Integer pageNum, Integer pageSize) {
        Integer startRow = (pageNum - 1) * pageSize;
        return marketingMapper.selectPage(name, type, status, startRow, pageSize);
    }

    @Override
    public int getMarketingCount(String name, String type, Integer status) {
        return marketingMapper.count(name, type, status);
    }

    @Override
    public Marketing createMarketing(Marketing marketing) {
        marketing.setCreateTime(new Date());
        marketing.setUpdateTime(new Date());
        marketingMapper.insert(marketing);
        return marketing;
    }

    @Override
    public Marketing updateMarketing(Marketing marketing) {
        marketing.setUpdateTime(new Date());
        marketingMapper.update(marketing);
        return marketing;
    }

    @Override
    public void deleteMarketing(Long id) {
        marketingMapper.deleteById(id);
    }

    @Override
    public List<Marketing> getMarketingByType(String type) {
        return marketingMapper.selectByType(type);
    }

    @Override
    public List<Marketing> getMarketingByStatus(Integer status) {
        return marketingMapper.selectByStatus(status);
    }
}
package com.oa.business.service.impl;

import com.oa.business.dto.PageResponse;
import com.oa.business.entity.Marketing;
import com.oa.business.mapper.MarketingMapper;
import com.oa.business.service.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketingServiceImpl implements MarketingService {

    @Autowired
    private MarketingMapper marketingMapper;

    @Override
    public Marketing getMarketingById(Long id) {
        return marketingMapper.selectById(id);
    }

    @Override
    public List<Marketing> getAllMarketings(String sortOrder) {
        return marketingMapper.selectAll(sortOrder);
    }

    @Override
    public PageResponse<Marketing> getMarketingsPage(int pageNum, int pageSize, String sortOrder) {
        List<Marketing> all = marketingMapper.selectAll(sortOrder);
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<Marketing> records = start < all.size() ? all.subList(start, end) : List.of();
        return PageResponse.of(all.size(), pageNum, pageSize, records);
    }

    @Override
    public void createMarketing(Marketing marketing, Long createBy) {
        marketing.setCreateBy(createBy);
        marketing.setCreateTime(LocalDateTime.now());
        marketingMapper.insert(marketing);
    }

    @Override
    public void updateMarketing(Marketing marketing, Long updateBy) {
        marketing.setUpdateBy(updateBy);
        marketing.setUpdateTime(LocalDateTime.now());
        marketingMapper.update(marketing);
    }

    @Override
    public void deleteMarketing(Long id) {
        marketingMapper.deleteById(id);
    }
}

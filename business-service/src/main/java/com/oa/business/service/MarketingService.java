package com.oa.business.service;

import com.oa.business.entity.Marketing;
import com.oa.business.dto.PageResponse;

import java.util.List;

public interface MarketingService {
    Marketing getMarketingById(Long id);
    List<Marketing> getAllMarketings(String sortOrder);
    PageResponse<Marketing> getMarketingsPage(int pageNum, int pageSize, String sortOrder);
    void createMarketing(Marketing marketing, Long createBy);
    void updateMarketing(Marketing marketing, Long updateBy);
    void deleteMarketing(Long id);
}

package com.oa.system.service;

import com.oa.system.entity.Marketing;

import java.util.List;

/**
 * 营销活动服务接口
 */
public interface MarketingService {

    /**
     * 获取所有活动列表
     */
    List<Marketing> getAllMarketing();

    /**
     * 根据ID获取活动
     */
    Marketing getMarketingById(Long id);

    /**
     * 分页查询活动
     */
    List<Marketing> getMarketingPage(String name, String type, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    int getMarketingCount(String name, String type, Integer status);

    /**
     * 创建活动
     */
    Marketing createMarketing(Marketing marketing);

    /**
     * 更新活动
     */
    Marketing updateMarketing(Marketing marketing);

    /**
     * 删除活动
     */
    void deleteMarketing(Long id);

    /**
     * 根据类型获取活动列表
     */
    List<Marketing> getMarketingByType(String type);

    /**
     * 根据状态获取活动列表
     */
    List<Marketing> getMarketingByStatus(Integer status);
}
package com.oa.business.service;

import com.oa.business.dto.TrackingNode;

import java.util.List;

/**
 * 快递服务商查询接口
 * 不同的快递公司实现不同的查询逻辑
 */
public interface ExpressQueryService {

    /**
     * 实时查询快递轨迹
     * @param expressNo 运单号
     * @return 轨迹节点列表
     */
    List<TrackingNode> queryTracking(String expressNo);

    /**
     * 获取最新物流状态码
     * @param expressNo 运单号
     * @return 0-已揽收 1-运输中 2-派送中 3-已签收 4-异常
     */
    Integer getLatestStatus(String expressNo);

    /**
     * 快递公司代码
     */
    String getCompanyCode();
}

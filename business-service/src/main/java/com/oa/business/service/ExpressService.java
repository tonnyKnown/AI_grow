package com.oa.business.service;

import com.oa.business.dto.ExpressShippingRequest;
import com.oa.business.dto.PageResponse;
import com.oa.business.entity.Express;

import java.util.List;

public interface ExpressService {
    Express getByOrderId(Long orderId);
    Express getById(Long id);
    List<Express> getListByCondition(String orderNo, String expressCompany, Integer status);
    PageResponse<Express> getExpressPage(int pageNum, int pageSize, String orderNo, String expressCompany, Integer status);
    Express createExpress(ExpressShippingRequest request, Long createBy);
    void updateExpress(Express express, Long updateBy);
    void updateTracking(Long id, String trackingNodes, Long updateBy);
    void deleteExpress(Long id);
}

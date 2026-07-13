package com.oa.business.service.impl;

import com.oa.business.dto.ExpressShippingRequest;
import com.oa.business.dto.PageResponse;
import com.oa.business.entity.Express;
import com.oa.business.entity.Order;
import com.oa.business.mapper.ExpressMapper;
import com.oa.business.mapper.OrderMapper;
import com.oa.business.service.ExpressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpressServiceImpl implements ExpressService {

    @Autowired
    private ExpressMapper expressMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Express getByOrderId(Long orderId) {
        return expressMapper.selectByOrderId(orderId);
    }

    @Override
    public Express getById(Long id) {
        return expressMapper.selectById(id);
    }

    @Override
    public List<Express> getListByCondition(String orderNo, String expressCompany, Integer status) {
        return expressMapper.selectByCondition(orderNo, expressCompany, status);
    }

    @Override
    public PageResponse<Express> getExpressPage(int pageNum, int pageSize, String orderNo, String expressCompany, Integer status) {
        List<Express> all = expressMapper.selectByCondition(orderNo, expressCompany, status);
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<Express> records = start < all.size() ? all.subList(start, end) : List.of();
        return PageResponse.of(all.size(), pageNum, pageSize, records);
    }

    @Override
    @Transactional
    public Express createExpress(ExpressShippingRequest request, Long createBy) {
        // 查询订单信息
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查是否已存在物流记录
        Express existing = expressMapper.selectByOrderId(request.getOrderId());
        if (existing != null) {
            throw new RuntimeException("该订单已创建物流信息");
        }

        // 构建默认轨迹节点（含坐标）
        String senderLoc = request.getSenderAddress() != null ? request.getSenderAddress() : "";
        String defaultTracking = "[{\"time\":\"" + LocalDateTime.now().toString().replace("T", " ") + "\",\"desc\":\"包裹已揽收\",\"location\":\"" + senderLoc + "\",\"lat\":22.5431,\"lng\":114.0579}]";

        Express express = new Express();
        express.setOrderId(request.getOrderId());
        express.setOrderNo(order.getOrderNo());
        express.setExpressCompany(request.getExpressCompany());
        express.setExpressNo(request.getExpressNo());
        express.setStatus(0); // 已揽收
        express.setTrackingNodes(defaultTracking);
        express.setSenderName(request.getSenderName());
        express.setSenderPhone(request.getSenderPhone());
        express.setSenderAddress(request.getSenderAddress());
        express.setRemark(request.getRemark());
        express.setCreateBy(createBy);
        express.setCreateTime(LocalDateTime.now());

        expressMapper.insert(express);
        return express;
    }

    @Override
    public void updateExpress(Express express, Long updateBy) {
        express.setUpdateBy(updateBy);
        express.setUpdateTime(LocalDateTime.now());
        expressMapper.update(express);
    }

    @Override
    public void updateTracking(Long id, String trackingNodes, Long updateBy) {
        Express express = new Express();
        express.setId(id);
        express.setTrackingNodes(trackingNodes);
        express.setUpdateBy(updateBy);
        expressMapper.update(express);
    }

    @Override
    public void deleteExpress(Long id) {
        expressMapper.deleteById(id);
    }
}

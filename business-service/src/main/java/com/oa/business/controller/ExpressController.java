package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.dto.ExpressShippingRequest;
import com.oa.business.dto.PageResponse;
import com.oa.business.dto.TrackingNode;
import com.oa.business.entity.Express;
import com.oa.business.service.ExpressQueryService;
import com.oa.business.service.ExpressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/express")
public class ExpressController {

    @Autowired
    private ExpressService expressService;

    @Autowired
    private List<ExpressQueryService> queryServices;

    @GetMapping("/order/{orderId}")
    public Result<Express> getByOrderId(@PathVariable Long orderId) {
        Express express = expressService.getByOrderId(orderId);
        return Result.success(express);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getExpressList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String expressCompany,
            @RequestParam(required = false) Integer status) {
        List<Express> allList = expressService.getListByCondition(orderNo, expressCompany, status);
        int total = allList.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Express> records = start < total ? allList.subList(start, end) : List.of();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @PostMapping
    public Result<Void> createExpress(@RequestBody ExpressShippingRequest request,
                                       @RequestHeader(value = "userId") Long userId) {
        expressService.createExpress(request, userId);
        return Result.success("物流信息创建成功", null);
    }

    @PutMapping
    public Result<Void> updateExpress(@RequestBody Express express,
                                       @RequestHeader(value = "userId") Long userId) {
        expressService.updateExpress(express, userId);
        return Result.success("物流信息更新成功", null);
    }

    @PutMapping("/tracking")
    public Result<Void> updateTracking(@RequestBody Map<String, Object> params,
                                        @RequestHeader(value = "userId") Long userId) {
        Long id = Long.valueOf(params.get("id").toString());
        String trackingNodes = (String) params.get("trackingNodes");
        expressService.updateTracking(id, trackingNodes, userId);
        return Result.success("物流轨迹更新成功", null);
    }

    @GetMapping("/query/{id}")
    public Result<Map<String, Object>> queryRealTime(@PathVariable Long id) {
        Express express = expressService.getById(id);
        if (express == null) {
            return Result.error("物流记录不存在");
        }

        // 查找对应的快递查询服务
        ExpressQueryService queryService = null;
        for (ExpressQueryService qs : queryServices) {
            if (qs.getCompanyCode().equals(express.getExpressCompany())) {
                queryService = qs;
                break;
            }
        }

        if (queryService == null) {
            return Result.success("当前快递公司暂不支持实时查询", null);
        }

        // 实时查询最新轨迹
        List<TrackingNode> nodes = queryService.queryTracking(express.getExpressNo());
        Integer latestStatus = queryService.getLatestStatus(express.getExpressNo());

        Map<String, Object> result = new HashMap<>();
        result.put("trackingNodes", nodes);
        result.put("status", latestStatus);
        result.put("expressCompany", express.getExpressCompany());
        result.put("expressNo", express.getExpressNo());

        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteExpress(@PathVariable Long id) {
        expressService.deleteExpress(id);
        return Result.success("删除成功", null);
    }
}

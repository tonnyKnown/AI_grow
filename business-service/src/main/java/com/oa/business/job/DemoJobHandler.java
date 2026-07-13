package com.oa.business.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.business.dto.TrackingNode;
import com.oa.business.entity.Express;
import com.oa.business.mapper.ExpressMapper;
import com.oa.business.service.ExpressQueryService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务 - 物流状态同步
 * 定时查询各快递服务商的最新物流状态，更新到本地数据库
 */
@Component
public class DemoJobHandler {

    private static final Logger log = LoggerFactory.getLogger(DemoJobHandler.class);

    @Autowired
    private ExpressMapper expressMapper;

    @Autowired
    private List<ExpressQueryService> queryServices;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 同步快递物流状态（建议每30分钟执行一次）
     * 查询所有未签收/未异常的物流记录，调用快递服务商API获取最新轨迹
     */
    @XxlJob("syncExpressStatus")
    public ReturnT<String> syncExpressStatus(String param) {
        XxlJobHelper.log("========== 开始同步快递物流状态 ==========");

        try {
            // 1. 查询所有活跃物流记录（未签收且非异常）
            List<Express> activeList = expressMapper.selectActive();
            if (activeList.isEmpty()) {
                XxlJobHelper.log("当前无活跃物流记录需要同步");
                return ReturnT.SUCCESS;
            }
            XxlJobHelper.log("待同步物流记录数: {}", activeList.size());

            // 2. 按快递公司分组
            Map<String, List<Express>> grouped = activeList.stream()
                    .collect(Collectors.groupingBy(Express::getExpressCompany));

            // 3. 遍历每个快递公司，用对应的查询服务同步
            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<String, List<Express>> entry : grouped.entrySet()) {
                String company = entry.getKey();
                ExpressQueryService queryService = findQueryService(company);

                if (queryService == null) {
                    XxlJobHelper.log("未找到 {} 的查询服务实现，跳过", company);
                    continue;
                }

                for (Express express : entry.getValue()) {
                    try {
                        syncSingleExpress(express, queryService);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        XxlJobHelper.log("同步失败 - 运单号: {}, 错误: {}", express.getExpressNo(), e.getMessage());
                        log.error("同步物流状态失败 expressNo={}", express.getExpressNo(), e);
                    }
                }
            }

            XxlJobHelper.log("同步完成: 成功={}, 失败={}", successCount, failCount);
            XxlJobHelper.log("========== 快递物流状态同步结束 ==========");

            return ReturnT.SUCCESS;

        } catch (Exception e) {
            XxlJobHelper.log("物流状态同步任务执行失败: {}", e.getMessage());
            log.error("物流状态同步任务执行失败", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 同步单条物流记录
     */
    private void syncSingleExpress(Express express, ExpressQueryService queryService) throws JsonProcessingException {
        // 查询最新轨迹
        List<TrackingNode> latestNodes = queryService.queryTracking(express.getExpressNo());
        if (latestNodes == null || latestNodes.isEmpty()) {
            XxlJobHelper.log("运单号 {}: 未查询到轨迹数据", express.getExpressNo());
            return;
        }

        // 获取最新状态
        Integer latestStatus = queryService.getLatestStatus(express.getExpressNo());
        if (latestStatus == null) {
            latestStatus = express.getStatus();
        }

        // 序列化为JSON
        String trackingJson = objectMapper.writeValueAsString(latestNodes);

        // 更新数据库
        Express update = new Express();
        update.setId(express.getId());
        update.setStatus(latestStatus);
        update.setTrackingNodes(trackingJson);
        update.setUpdateBy(0L);
        update.setUpdateTime(LocalDateTime.now());
        expressMapper.update(update);

        XxlJobHelper.log("运单号 {}: 状态更新为 {} ({}), 轨迹节点数: {}",
                express.getExpressNo(), latestStatus, getStatusText(latestStatus), latestNodes.size());
    }

    /**
     * 查找对应的快递查询服务
     */
    private ExpressQueryService findQueryService(String company) {
        for (ExpressQueryService service : queryServices) {
            if (service.getCompanyCode().equals(company)) {
                return service;
            }
        }
        return null;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        String[] texts = {"已揽收", "运输中", "派送中", "已签收", "异常"};
        return status >= 0 && status < texts.length ? texts[status] : "未知";
    }
}

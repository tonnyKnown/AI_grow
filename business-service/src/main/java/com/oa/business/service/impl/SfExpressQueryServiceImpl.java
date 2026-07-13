package com.oa.business.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.business.dto.TrackingNode;
import com.oa.business.entity.Express;
import com.oa.business.mapper.ExpressMapper;
import com.oa.business.service.ExpressQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 顺丰速运 - 快递查询模拟实现
 *
 * 真实场景下应调用顺丰丰桥API或快递100聚合API:
 *   - 丰桥API: https://qiao.sf-express.com/
 *   - 快递100: https://api.kuaidi100.com/
 *
 * 当前实现根据发货时间模拟顺丰真实物流轨迹和时间线，
 * 坐标数据用于前端地图展示。
 */
@Service
public class SfExpressQueryServiceImpl implements ExpressQueryService {

    private static final Logger log = LoggerFactory.getLogger(SfExpressQueryServiceImpl.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ExpressMapper expressMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getCompanyCode() {
        return "顺丰速运";
    }

    /**
     * 模拟顺丰物流轨迹查询
     * 根据距离发货时间的小时数生成不同阶段的轨迹
     */
    @Override
    public List<TrackingNode> queryTracking(String expressNo) {
        // 从数据库查物流记录获取发货时间
        Express express = expressMapper.selectByOrderNo(expressNo);
        if (express == null) {
            return generateEmptyTracking();
        }
        return generateTrackingByElapsedHours(express);
    }

    /**
     * 根据发货时间计算所处物流阶段
     */
    @Override
    public Integer getLatestStatus(String expressNo) {
        Express express = expressMapper.selectByOrderNo(expressNo);
        if (express == null) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime shipTime = express.getCreateTime();
        if (shipTime == null) return 0;

        long hours = Duration.between(shipTime, now).toHours();

        if (hours < 2) return 0;          // 已揽收
        if (hours < 12) return 1;          // 运输中
        if (hours < 24) return 2;          // 派送中
        return 3;                          // 已签收
    }

    /**
     * 根据发货时间生成模拟轨迹
     * 包含真实的经纬度坐标（深圳-广州-上海-北京典型线路）
     */
    private List<TrackingNode> generateTrackingByElapsedHours(Express express) {
        LocalDateTime shipTime = express.getCreateTime() != null ? express.getCreateTime() : LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();
        long hours = Duration.between(shipTime, now).toHours();

        List<TrackingNode> nodes = new ArrayList<>();

        // 如果已有轨迹节点，尝试解析并扩展
        List<TrackingNode> existing = parseExistingNodes(express.getTrackingNodes());
        if (!existing.isEmpty()) {
            // 保留已有节点，追加新生成的节点
            nodes.addAll(existing);
        }

        // 生成模拟轨迹 - 深圳 → 广州 → 上海 → 北京
        // 坐标数据来自高德地图经纬度

        // 节点1: 已揽收（始终存在）
        String shipTimeStr = shipTime.format(DTF);
        if (!hasNodeWithDesc(nodes, "包裹已揽收")) {
            nodes.add(new TrackingNode(shipTimeStr, "包裹已揽收",
                    express.getSenderAddress() != null ? express.getSenderAddress() : "深圳",
                    22.5431, 114.0579));
        }

        if (hours >= 1) {
            LocalDateTime t1 = shipTime.plusHours(1);
            if (!hasNodeWithDesc(nodes, "快件已到达深圳分拣中心")) {
                nodes.add(new TrackingNode(t1.format(DTF), "快件已到达深圳分拣中心",
                        "广东省深圳市宝安区福永分拣中心", 22.6332, 113.8292));
            }
        }

        if (hours >= 3) {
            LocalDateTime t2 = shipTime.plusHours(3);
            if (!hasNodeWithDesc(nodes, "快件已离开深圳分拣中心，发往广州中转")) {
                nodes.add(new TrackingNode(t2.format(DTF), "快件已离开深圳分拣中心，发往广州中转",
                        "广东省深圳市", 22.5431, 114.0579));
            }
        }

        if (hours >= 5) {
            LocalDateTime t3 = shipTime.plusHours(5);
            if (!hasNodeWithDesc(nodes, "快件已到达广州中转中心")) {
                nodes.add(new TrackingNode(t3.format(DTF), "快件已到达广州中转中心",
                        "广东省广州市白云区太和分拨中心", 23.3525, 113.3114));
            }
        }

        if (hours >= 8) {
            LocalDateTime t4 = shipTime.plusHours(8);
            if (!hasNodeWithDesc(nodes, "快件已离开广州，发往上海")) {
                nodes.add(new TrackingNode(t4.format(DTF), "快件已离开广州，发往上海",
                        "广东省广州市", 23.3525, 113.3114));
            }
        }

        if (hours >= 14) {
            LocalDateTime t5 = shipTime.plusHours(14);
            if (!hasNodeWithDesc(nodes, "快件已到达上海浦东中转中心")) {
                nodes.add(new TrackingNode(t5.format(DTF), "快件已到达上海浦东中转中心",
                        "上海市浦东新区合庆分拨中心", 31.2422, 121.7038));
            }
        }

        if (hours >= 18) {
            LocalDateTime t6 = shipTime.plusHours(18);
            if (!hasNodeWithDesc(nodes, "快件已离开上海，发往北京")) {
                nodes.add(new TrackingNode(t6.format(DTF), "快件已离开上海，发往北京",
                        "上海市", 31.2304, 121.4737));
            }
        }

        if (hours >= 24) {
            LocalDateTime t7 = shipTime.plusHours(24);
            if (!hasNodeWithDesc(nodes, "快件已到达北京分拣中心")) {
                nodes.add(new TrackingNode(t7.format(DTF), "快件已到达北京分拣中心",
                        "北京市大兴区京南分拨中心", 39.7226, 116.3312));
            }
        }

        if (hours >= 28) {
            LocalDateTime t8 = shipTime.plusHours(28);
            if (!hasNodeWithDesc(nodes, "快递员已揽件，正在派送中")) {
                nodes.add(new TrackingNode(t8.format(DTF), "快递员已揽件，正在派送中",
                        "北京市朝阳区", 39.9219, 116.4435));
            }
        }

        if (hours >= 36) {
            LocalDateTime t9 = shipTime.plusHours(36);
            if (!hasNodeWithDesc(nodes, "包裹已签收")) {
                String receiverAddr = express.getSenderAddress() != null ? express.getSenderAddress() : "北京市朝阳区";
                nodes.add(new TrackingNode(t9.format(DTF), "包裹已签收，感谢使用顺丰速运",
                        receiverAddr, 39.9219, 116.4435));
            }
        }

        return nodes;
    }

    private List<TrackingNode> generateEmptyTracking() {
        List<TrackingNode> nodes = new ArrayList<>();
        nodes.add(new TrackingNode(
                LocalDateTime.now().format(DTF),
                "运单已创建，等待快递员揽收",
                "暂无位置信息"
        ));
        return nodes;
    }

    private boolean hasNodeWithDesc(List<TrackingNode> nodes, String desc) {
        return nodes.stream().anyMatch(n -> desc.equals(n.getDesc()));
    }

    private List<TrackingNode> parseExistingNodes(String trackingNodesJson) {
        if (trackingNodesJson == null || trackingNodesJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(trackingNodesJson,
                    new TypeReference<List<TrackingNode>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}

-- 物流管理功能 - 数据库迁移脚本
-- 使用方法: 在MySQL中执行此脚本

CREATE TABLE IF NOT EXISTS `sys_express` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物流ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `express_company` varchar(50) NOT NULL COMMENT '快递公司',
  `express_no` varchar(100) NOT NULL COMMENT '运单号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '物流状态：0-已揽收 1-运输中 2-派送中 3-已签收 4-异常',
  `tracking_nodes` json DEFAULT NULL COMMENT '物流轨迹节点JSON',
  `sender_name` varchar(50) DEFAULT NULL COMMENT '发件人',
  `sender_phone` varchar(20) DEFAULT NULL COMMENT '发件人电话',
  `sender_address` varchar(255) DEFAULT NULL COMMENT '发件地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_express_no` (`express_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流信息表';

-- 在业务管理菜单下添加物流管理子菜单（parent_id需根据实际情况调整）
INSERT INTO `sys_menu` (`menu_name`, `path`, `component`, `icon`, `parent_id`, `order_num`, `role_keys`, `status`, `create_by`, `create_time`, `remark`)
SELECT '物流管理', '/business/logistics', '@/views/LogisticsManagement.vue', 'Van', 7, 7, 'admin,user', 1, '1', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `path` = '/business/logistics');

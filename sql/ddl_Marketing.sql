CREATE TABLE IF NOT EXISTS marketing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(200) NOT NULL COMMENT '活动名称',
    type VARCHAR(50) NOT NULL COMMENT '活动类型',
    description TEXT COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status INT DEFAULT 1 COMMENT '活动状态：0-禁用，1-启用',
    rules TEXT COMMENT '活动规则（JSON格式）',
    product_ids TEXT COMMENT '参与商品范围',
    create_by VARCHAR(100) COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(100) COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注信息',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动表';
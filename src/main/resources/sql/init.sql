-- =============================================
-- 系统管理数据库建表脚本
-- =============================================

-- =============================================
-- 1. 系统用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键，自增',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名，登录账号，唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，加密存储',
    `email` VARCHAR(100) COMMENT '电子邮箱',
    `phone` VARCHAR(20) COMMENT '手机号码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `avatar` VARCHAR(500) COMMENT '用户头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '用户状态 0禁用 1启用',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';


-- =============================================
-- 2. 系统角色表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID，主键，自增',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称，用于显示',
    `role_key` VARCHAR(100) NOT NULL COMMENT '角色标识，用于权限控制，唯一',
    `description` VARCHAR(500) COMMENT '角色描述信息',
    `status` TINYINT DEFAULT 1 COMMENT '角色状态 0禁用 1启用',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';


-- =============================================
-- 3. 系统权限表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID，主键，自增',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_key` VARCHAR(100) NOT NULL COMMENT '权限标识，用于权限控制，唯一',
    `resource_type` VARCHAR(20) COMMENT '资源类型 menu菜单 button按钮 api接口',
    `path` VARCHAR(200) COMMENT '前端路由路径',
    `component` VARCHAR(200) COMMENT '前端组件路径',
    `parent_id` BIGINT COMMENT '父级权限ID，顶级权限为null',
    `order_num` INT DEFAULT 0 COMMENT '排序号，数值越小越靠前',
    `status` TINYINT DEFAULT 1 COMMENT '权限状态 0禁用 1启用',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_key` (`permission_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';


-- =============================================
-- 4. 系统菜单表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID，主键，自增',
    `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(200) COMMENT '前端路由路径',
    `component` VARCHAR(200) COMMENT '前端组件路径',
    `icon` VARCHAR(100) COMMENT '菜单图标',
    `parent_id` BIGINT COMMENT '父级菜单ID，顶级菜单为null',
    `order_num` INT DEFAULT 0 COMMENT '排序号，数值越小越靠前',
    `role_keys` VARCHAR(500) COMMENT '可见角色标识列表，逗号分隔，为空表示所有角色可见',
    `status` TINYINT DEFAULT 1 COMMENT '菜单状态 0禁用 1启用',
    `create_by` VARCHAR(100) COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` VARCHAR(100) COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';


-- =============================================
-- 5. 商品分类表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键，自增',
    `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(100) NOT NULL COMMENT '分类编码，唯一标识',
    `sort` INT DEFAULT 0 COMMENT '排序号，数值越小越靠前',
    `status` TINYINT DEFAULT 1 COMMENT '分类状态 0禁用 1启用',
    `parent_id` BIGINT COMMENT '父级分类ID，顶级分类为null',
    `remark` VARCHAR(500) COMMENT '备注信息',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';


-- =============================================
-- 6. 商品表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID，主键，自增',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `product_code` VARCHAR(100) COMMENT '商品编码，唯一标识',
    `category` VARCHAR(100) COMMENT '商品分类',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格，保留两位小数',
    `stock` INT DEFAULT 0 COMMENT '商品库存数量',
    `description` TEXT COMMENT '商品描述',
    `image_url` VARCHAR(500) COMMENT '商品图片URL',
    `status` TINYINT DEFAULT 1 COMMENT '商品状态 0下架 1上架',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';


-- =============================================
-- 7. 订单表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键，自增',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，唯一标识',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) COMMENT '商品名称',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT DEFAULT 1 COMMENT '订单状态 1待发货 2已发货 3配送中 4已收货 5已完成 6已取消 7退货中 8已退货 9已退款',
    `shipping_address` VARCHAR(500) COMMENT '收货地址',
    `receiver_name` VARCHAR(50) COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) COMMENT '收货人手机号',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `update_time` DATETIME COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';


-- =============================================
-- 8. 用户角色关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键，自增',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `role_name` VARCHAR(100) COMMENT '角色名称（冗余字段，用于查询优化）',
    `role_key` VARCHAR(100) COMMENT '角色标识（冗余字段，用于查询优化）',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


-- =============================================
-- 9. 角色权限关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键，自增',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';


-- =============================================
-- 10. 角色菜单关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键，自增',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';


-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认管理员用户（密码：admin123，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`, `create_time`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, NOW());

-- 插入默认角色
INSERT INTO `sys_role` (`role_name`, `role_key`, `description`, `status`, `create_time`) VALUES
('超级管理员', 'admin', '拥有系统所有权限', 1, NOW()),
('普通用户', 'user', '普通用户权限', 1, NOW());

-- 为admin用户分配超级管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `role_name`, `role_key`, `create_time`) VALUES
(1, 1, '超级管理员', 'admin', NOW());

-- 插入默认菜单
INSERT INTO `sys_menu` (`menu_name`, `path`, `component`, `icon`, `parent_id`, `order_num`, `status`, `create_time`) VALUES
('首页', '/dashboard', 'Dashboard', 'House', NULL, 1, 1, NOW()),
('系统管理', '/system', '', 'Setting', NULL, 2, 1, NOW()),
('用户管理', '/system/users', 'UserManagement', 'User', 2, 1, 1, NOW()),
('角色管理', '/system/roles', 'RoleManagement', 'UserFilled', 2, 2, 1, NOW()),
('权限管理', '/system/permissions', 'PermissionManagement', 'Key', 2, 3, 1, NOW()),
('菜单管理', '/system/menus', 'MenuManagement', 'Menu', 2, 4, 1, NOW()),
('商品管理', '/products', '', 'Goods', NULL, 3, 1, NOW()),
('商品列表', '/products/list', 'ProductManagement', 'List', 7, 1, 1, NOW()),
('分类管理', '/products/categories', 'CategoryManagement', 'FolderOpened', 7, 2, 1, NOW()),
('订单管理', '/orders', 'OrderManagement', 'Document', NULL, 4, 1, NOW());

CREATE DATABASE IF NOT EXISTS `example_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `example_db`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清理本地源库已不存在的历史表
DROP TABLE IF EXISTS `sys_role_menu`;

-- 用户表

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(255) NOT NULL COMMENT '登录密码密文',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `status` int NOT NULL COMMENT '状态：0禁用，1启用',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uc_sys_user_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
INSERT INTO `sys_user` (`id`, `username`, `password`, `email`, `phone`, `real_name`, `avatar`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1,'admin','$2a$10$n2d.V558q/PCJKJ/rR4QX.BB6GNmmoNed4SEUs66sw28wa6BcjWkS','285578779@qq.com','18098983413','张三',NULL,1,1,'2026-05-09 20:33:06',1,'2026-05-09 20:33:18','da'),(2,'test','$2a$10$D20kWuoc3FeN39NdWGQkyOGsrKFRlZC9GD3Aj0NkFeRD1peGYCRUy','','18098984432','李四',NULL,1,1,'2026-05-09 21:15:05',NULL,NULL,NULL),(3,'wang','$2a$10$WceQgXKd93SZyfyElfNff.KWDFE5n3EMKpfAU6kquge1PkJGPf9ce','','','王五',NULL,1,1,'2026-05-10 01:24:39',NULL,NULL,NULL);

-- 角色表

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色标识',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` int NOT NULL COMMENT '状态：0禁用，1启用',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `description`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1,'管理员','admin','可以查看所有权限',1,1,'2026-05-09 21:21:59',NULL,NULL,NULL),(2,'vip用户','user','普通用户',1,1,'2026-05-09 22:34:59',NULL,NULL,NULL);

-- 权限表

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `permission_key` varchar(100) NOT NULL COMMENT '权限标识',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型',
  `path` varchar(100) DEFAULT NULL COMMENT '资源路径',
  `component` varchar(100) DEFAULT NULL COMMENT '前端组件',
  `parent_id` bigint DEFAULT NULL COMMENT '父级权限ID',
  `order_num` int NOT NULL COMMENT '排序',
  `status` int NOT NULL COMMENT '状态：0禁用，1启用',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_key`, `resource_type`, `path`, `component`, `parent_id`, `order_num`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6,'普通用户','user','menu','','',0,0,1,1,'2026-05-10 00:12:37',1,'2026-05-10 01:15:59',NULL),(7,'超级用户','admin','menu','','',0,0,1,1,'2026-05-10 00:13:39',1,'2026-05-10 01:16:08',NULL);

-- 菜单表

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `path` varchar(255) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `parent_id` bigint DEFAULT '0' COMMENT '父级菜单ID',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序',
  `role_keys` varchar(500) DEFAULT NULL COMMENT '可访问角色标识，多个用逗号分隔',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `create_by` varchar(50) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';
INSERT INTO `sys_menu` (`id`, `menu_name`, `path`, `component`, `icon`, `parent_id`, `order_num`, `role_keys`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1,'首页','/dashboard','Layout.vue','HomeFilled',NULL,1,'admin',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:16:40','首页入口'),(2,'系统管理','/system','','Setting',NULL,2,'admin',1,'1','2026-05-10 00:47:07',NULL,'2026-05-10 01:17:23','系统管理目录'),(3,'用户管理','/system/users','@/views/UserManagement.vue','User',2,1,'admin',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:12:40',''),(4,'角色管理','/system/roles','@/views/RoleManagement.vue','UserFilled',2,2,'admin',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:12:44',''),(5,'权限管理','/system/permissions','@/views/PermissionManagement.vue','Lock',2,3,'admin',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:12:47',''),(6,'菜单管理','/system/menus','@/views/MenuManagement.vue','Menu',2,4,'admin',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:12:50',''),(7,'业务管理','/business','','Goods',NULL,3,'admin,user',1,'1','2026-05-10 00:47:07',NULL,'2026-05-10 01:17:58','业务管理目录'),(8,'商品管理','/business/products','@/views/ProductManagement.vue','Goods',7,1,'admin,user',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:13:23',''),(9,'订单管理','/business/orders','@/views/OrderManagement.vue','Document',7,2,'admin,user',1,'1','2026-05-10 00:47:07','1','2026-05-23 10:13:28',''),(10,'营销推广','/business/marketing','@/views/MarketingManagement.vue','',7,3,'admin,user',1,'1','2026-05-10 17:45:43','1','2026-05-23 10:13:32',''),(11,'知识百科智能','/business/chat','@/views/ChatBot.vue','',7,4,'admin,user',1,'1','2026-05-13 18:11:07','1','2026-05-23 10:13:38',NULL),(12,'公司业务助手','/business/knowledge','@/views/KnowledgeBot.vue','',7,5,'admin,user',1,'1','2026-05-20 02:10:37','1','2026-05-23 10:13:42',NULL),(13,'智能体','/business/javachain','@/views/JavaChain.vue','',7,6,'admin,user',1,'1','2026-06-01 20:57:20',NULL,NULL,NULL);

-- 用户角色关系表

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关系表';
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`, `create_time`) VALUES (1,1,1,1,'2026-05-09 22:30:28'),(3,2,2,1,'2026-05-10 01:15:26'),(4,3,2,1,'2026-05-10 01:24:39');

-- 角色权限关系表

DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关系表';
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_by`, `create_time`) VALUES (23,1,6,1,'2026-05-10 00:13:48'),(24,1,7,1,'2026-05-10 00:13:48'),(25,2,6,1,'2026-05-10 01:16:19');

-- 商品分类表

DROP TABLE IF EXISTS `sys_category`;
CREATE TABLE `sys_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_code` varchar(100) NOT NULL COMMENT '分类编码',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态 0禁用 1启用',
  `parent_id` bigint DEFAULT NULL COMMENT '父级分类ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `category_code` (`category_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';
INSERT INTO `sys_category` (`id`, `category_name`, `category_code`, `sort`, `status`, `parent_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1,'衣着类','1001',0,1,NULL,'',1,'2026-05-10 03:14:06',NULL,NULL),(3,'酒类','2001',1,1,NULL,'',1,'2026-05-10 03:15:24',NULL,NULL),(4,'水果','3001',2,1,NULL,'',1,'2026-05-10 03:39:47',1,'2026-05-10 03:40:02');

-- 商品表

DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `product_code` varchar(50) DEFAULT NULL COMMENT '商品编码',
  `category` varchar(50) DEFAULT NULL COMMENT '商品分类',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `stock` int DEFAULT NULL COMMENT '库存数量',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `image_url` varchar(255) DEFAULT NULL COMMENT '商品图片地址',
  `status` int NOT NULL COMMENT '状态：0禁用，1启用',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
INSERT INTO `sys_product` (`id`, `product_name`, `product_code`, `category`, `price`, `stock`, `description`, `image_url`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1,'鞋子','0222154','衣着类',7.00,10,'21额2e',NULL,1,1,'2026-05-09 21:16:40',1,'2026-06-01 20:37:11',NULL),(2,'外套','0221555','酒类',4.00,15,'testes',NULL,1,1,'2026-05-09 22:13:54',1,'2026-05-10 04:53:32',NULL),(3,'青岛啤酒','1231231','酒类',12.00,1215,'',NULL,1,1,'2026-05-10 02:58:44',1,'2026-05-10 04:53:32',NULL),(4,'百威','1231232','酒类',5.00,1168,'',NULL,1,1,'2026-05-10 03:16:06',1,'2026-05-10 04:53:32',NULL),(5,'喜力啤酒','1231233','酒类',10.00,12422,'',NULL,1,1,'2026-05-06 03:16:54',1,'2026-05-10 04:53:32',NULL),(6,'香蕉','30001','水果',2.00,49999,'',NULL,1,1,'2026-05-10 03:43:06',1,'2026-05-10 04:53:32',NULL),(7,'苹果','30002','水果',3.00,4242,'',NULL,1,1,'2026-05-10 03:50:31',1,'2026-05-10 03:50:46',NULL);

-- 订单表

DROP TABLE IF EXISTS `sys_order`;
CREATE TABLE `sys_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint DEFAULT NULL COMMENT '下单用户ID',
  `product_id` bigint DEFAULT NULL COMMENT '商品ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称快照',
  `quantity` int DEFAULT '1' COMMENT '购买数量',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '商品单价',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总金额',
  `status` int DEFAULT '1' COMMENT '订单状态',
  `shipping_address` text COMMENT '收货地址',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人手机号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
INSERT INTO `sys_order` (`id`, `order_no`, `user_id`, `product_id`, `product_name`, `quantity`, `unit_price`, `total_amount`, `status`, `shipping_address`, `receiver_name`, `receiver_phone`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1,'ORD177833578742647DC3456',1,1,'鞋子',1,5.00,5.00,3,'东莞','段门兴','18098983413','测试',1,'2026-05-09 22:09:47',1,'2026-05-09 22:27:25'),(3,'ORD17783475235488364702C',1,2,'外套',1,4.00,4.00,2,'深证','王五','18098983413','21312',1,'2026-05-10 01:25:24',1,'2026-05-10 02:46:35'),(4,'ORD17783547276525F693A94',1,4,'百威',50,5.00,250.00,1,'的萨芬萨','王五','18098983413','黑涩会',1,'2026-05-10 03:25:28',NULL,NULL),(5,'ORD1778360011790A9F4DADF',1,5,'喜力啤酒',2,10.00,20.00,8,'北京市朝阳区建国路88号','张三','13800138001','加急处理',1,'2026-05-10 04:53:32',1,'2026-07-06 17:35:19'),(6,'ORD1778360011829F2AAA84F',3,4,'百威',2,5.00,10.00,1,'上海市浦东新区世纪大道100号','李四','13800138002','',1,'2026-05-10 04:53:32',NULL,NULL),(7,'ORD1778360011852F8DC8E0E',1,6,'香蕉',1,2.00,2.00,4,'广州市天河区珠江新城69号','王五','13800138003','请务必送货上门',1,'2026-05-10 04:53:32',1,'2026-05-22 03:38:00'),(8,'ORD177836001187461DA8A81',1,4,'百威',1,5.00,5.00,2,'深圳市南山区科技园99号','赵六','13800138004','',1,'2026-05-10 04:53:32',1,'2026-05-22 03:39:16'),(9,'ORD177836001189821F9EDDD',2,4,'百威',1,5.00,5.00,3,'杭州市西湖区文三路478号','钱七','13800138005','周末可派送',1,'2026-05-10 04:53:32',1,'2026-07-06 09:35:47'),(10,'ORD17783600119213AD392C2',2,2,'外套',1,4.00,4.00,7,'成都市武侯区天府大道1号','孙八','13800138006','',1,'2026-05-10 04:53:32',NULL,'2026-05-24 12:36:13'),(12,'ORD1778360011971FA03D9C8',1,1,'鞋子',1,5.00,5.00,1,'南京市鼓楼区中山路100号','吴十','13800138008','',1,'2026-05-10 04:53:32',NULL,NULL),(13,'ORD1778360011994B2E41FAF',3,3,'青岛啤酒',5,12.00,60.00,2,'西安市雁塔区长安南路4号','郑一','13800138009','麻烦尽快发货',1,'2026-05-10 04:53:32',1,'2026-05-22 05:46:22'),(14,'ORD1778360012017824E3C02',3,3,'青岛啤酒',2,12.00,24.00,3,'重庆市渝中区解放碑88号','王二','13800138010','',1,'2026-05-10 04:53:32',1,'2026-05-22 05:45:53');

-- 营销活动表

DROP TABLE IF EXISTS `marketing`;
CREATE TABLE `marketing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动类型',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` int DEFAULT '1' COMMENT '活动状态：0-禁用，1-启用',
  `rules` text COLLATE utf8mb4_unicode_ci COMMENT '活动规则（JSON格式）',
  `product_ids` text COLLATE utf8mb4_unicode_ci COMMENT '参与商品范围',
  `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动表';
INSERT INTO `marketing` VALUES (1,'618年中大促','group','619购物节，全场商品限时优惠','2026-06-18 00:00:00','2026-06-20 23:59:59',1,'{\"discount\": 0.85}',NULL,'2','2026-05-10 18:11:48','1','2026-05-22 05:47:23',NULL),(2,'限时秒杀专场','flash','每日10点限时秒杀，低至5折','2026-05-15 10:00:00','2026-05-15 12:00:00',0,'{\"max_quantity\": 100}',NULL,'2','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','已结束'),(3,'三人团购优惠','group','三人成团享7折优惠，人数不够可拼团','2026-05-20 00:00:00','2026-05-25 23:59:59',0,'{\"min_members\": 3, \"discount\": 0.7}',NULL,'2','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21',''),(4,'新人专享优惠券','coupon','新用户注册即可领取专属优惠券','2026-01-01 00:00:00','2026-12-31 23:59:59',1,'{\"coupon_value\": 50}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','长期活动'),(5,'双11狂欢节','discount','双11全场75折，预热活动已开启','2026-11-11 00:00:00','2026-11-12 23:59:59',0,'{\"discount\": 0.75}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','年度最大活动'),(6,'夏日清凉大促','discount','夏季商品清凉价，满200减50','2026-07-01 00:00:00','2026-07-31 23:59:59',1,'{\"threshold\": 200, \"reduction\": 50}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21',''),(7,'周末闪购','flash','每周五六日限时抢购，数量有限','2026-05-01 00:00:00','2026-05-31 23:59:59',1,'{\"max_quantity\": 50}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','每周重复'),(8,'会员专享日','group','会员专属折扣，等级越高优惠越大','2026-06-15 00:00:00','2026-06-15 23:59:59',1,'{\"vip_discount\": 0.8}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','每月15日'),(9,'生日特惠礼包','coupon','生日当月享受双倍积分和专属折扣','2026-01-01 00:00:00','2026-12-31 23:59:59',1,'{\"bonus_points\": 2}',NULL,'2','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','长期有效'),(10,'开学季促销','discount','学生专属优惠，购买学习用品享8折','2026-09-01 00:00:00','2026-09-15 23:59:59',0,'{\"discount\": 0.8}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21',''),(11,'春节不打烊','discount','春节期间正常发货，满300包邮','2026-01-28 00:00:00','2026-02-04 23:59:59',0,'{\"free_shipping_threshold\": 300}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21','节假日活动'),(12,'品牌日狂欢','group','各大品牌联合促销，品质保证','2026-08-18 00:00:00','2026-08-20 23:59:59',0,'{\"brand_discount\": 0.75}',NULL,'1','2026-05-10 18:11:48',NULL,'2026-05-10 18:19:21',''),(14,'test','group','12','2026-05-18 00:00:00','2026-05-21 00:00:00',1,NULL,NULL,'1','2026-05-10 18:12:55','1','2026-05-10 18:12:55','4124'),(15,'test','group','1222222','2026-05-18 00:00:00','2026-05-21 00:00:00',1,NULL,NULL,'1','2026-05-10 18:13:21','1','2026-05-10 18:16:37','4'),(16,'1111','discount','数量优先','2026-05-03 16:00:00','2026-05-27 16:00:00',1,NULL,NULL,'1','2026-05-22 05:47:37','1','2026-05-22 05:52:52',NULL);

SET FOREIGN_KEY_CHECKS = 1;

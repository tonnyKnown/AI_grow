-- =============================================
-- 测试数据
-- =============================================

-- 插入商品分类
INSERT INTO `sys_category` (`category_name`, `category_code`, `sort`, `status`, `create_time`) VALUES
('食品饮料', 'FOOD', 1, 1, NOW()),
('电子产品', 'ELECTRONICS', 2, 1, NOW()),
('家居用品', 'HOME', 3, 1, NOW());

-- 插入商品数据
INSERT INTO `sys_product` (`product_name`, `product_code`, `category`, `price`, `stock`, `description`, `status`, `create_time`) VALUES
('可口可乐 330ml', 'COKE001', '食品饮料', 3.50, 100, '经典可口可乐，330ml罐装', 1, NOW()),
('农夫山泉 550ml', 'WATER001', '食品饮料', 2.00, 200, '天然矿泉水，550ml瓶装', 1, NOW()),
('乐事薯片 原味 75g', 'CHIPS001', '食品饮料', 5.80, 150, '乐事薯片，原味，75g包装', 1, NOW()),
('小米蓝牙耳机 Air2', 'HEADSET001', '电子产品', 399.00, 50, '小米真无线蓝牙耳机，降噪功能', 1, NOW()),
('iPhone 15 Pro Max', 'PHONE001', '电子产品', 9999.00, 20, 'Apple iPhone 15 Pro Max 256GB', 1, NOW()),
('小米台灯 Pro', 'LAMP001', '家居用品', 199.00, 80, '小米智能台灯，护眼模式', 1, NOW()),
('宜家保温杯 500ml', 'CUP001', '家居用品', 49.00, 120, '宜家保温杯，500ml容量', 1, NOW());

-- 插入订单测试数据
INSERT INTO `sys_order` (`order_no`, `user_id`, `product_id`, `product_name`, `quantity`, `unit_price`, `total_amount`, `status`, `shipping_address`, `receiver_name`, `receiver_phone`, `remark`, `create_by`, `create_time`) VALUES
('ORD1747900000001', 1, 1, '可口可乐 330ml', 10, 3.50, 35.00, 1, '北京市朝阳区建国路88号SOHO现代城A座', '张三', '13800138001', '请尽快发货', 1, NOW()),
('ORD1747900000002', 1, 4, '小米蓝牙耳机 Air2', 2, 399.00, 798.00, 2, '上海市浦东新区张江高科技园区博云路2号', '李四', '13900139002', '买给朋友的生日礼物', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('ORD1747900000003', 1, 7, '宜家保温杯 500ml', 5, 49.00, 245.00, 3, '广州市天河区珠江新城华夏路10号富力中心', '王五', '13700137003', '', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('ORD1747900000004', 1, 2, '农夫山泉 550ml', 24, 2.00, 48.00, 1, '深圳市南山区科技园南区科苑南路1号', '赵六', '13600136004', '公司采购，送到前台', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('ORD1747900000005', 1, 5, 'iPhone 15 Pro Max', 1, 9999.00, 9999.00, 4, '杭州市西湖区文三路398号东方通信大厦', '钱七', '13500135005', '生日礼物，包装好看点', 1, DATE_SUB(NOW(), INTERVAL 7 DAY));

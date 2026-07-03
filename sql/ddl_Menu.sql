CREATE TABLE sys_menu
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    menu_name     VARCHAR(100)         NOT NULL,
    path          VARCHAR(255)         NULL,
    component     VARCHAR(255)         NULL,
    icon          VARCHAR(100)         NULL,
    parent_id     BIGINT                NULL DEFAULT 0,
    order_num     INT                   NOT NULL DEFAULT 0,
    role_keys     VARCHAR(500)          NULL,
    status        INT                   NOT NULL DEFAULT 1,
    create_by     VARCHAR(50)           NOT NULL,
    create_time   datetime              NOT NULL,
    update_by     VARCHAR(50)           NULL,
    update_time   datetime              NULL,
    remark        VARCHAR(500)          NULL,
    CONSTRAINT pk_sys_menu PRIMARY KEY (id)
);

-- 清空现有数据
TRUNCATE TABLE sys_menu;

-- 插入菜单数据
INSERT INTO sys_menu (menu_name, path, component, icon, parent_id, order_num, role_keys, status, create_by, create_time, remark) VALUES
                                                                                                                                     ('首页', '/dashboard', 'Layout.vue', 'HomeFilled', 0, 1, '', 1, 1, NOW(), '首页入口'),
                                                                                                                                     ('系统管理', '/system', '', 'Setting', 0, 2, 'admin', 1, 1, NOW(), '系统管理目录'),
                                                                                                                                     ('用户管理', '/users', '@/views/UserManagement.vue', 'User', 2, 1, 'admin', 1, 1, NOW(), ''),
                                                                                                                                     ('角色管理', '/roles', '@/views/RoleManagement.vue', 'UserFilled', 2, 2, 'admin', 1, 1, NOW(), ''),
                                                                                                                                     ('权限管理', '/permissions', '@/views/PermissionManagement.vue', 'Lock', 2, 3, 'admin', 1, 1, NOW(), ''),
                                                                                                                                     ('菜单管理', '/menus', '@/views/MenuManagement.vue', 'Menu', 2, 4, 'admin', 1, 1, NOW(), ''),
                                                                                                                                     ('业务管理', '/business', '', 'Goods', 0, 3, 'admin', 1, 1, NOW(), '业务管理目录'),
                                                                                                                                     ('商品管理', '/products', '@/views/ProductManagement.vue', 'Goods', 7, 1, 'admin', 1, 1, NOW(), ''),
                                                                                                                                     ('订单管理', '/orders', '@/views/OrderManagement.vue', 'Document', 7, 2, 'admin', 1, 1, NOW(), '');
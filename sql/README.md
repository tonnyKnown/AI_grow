# SQL 数据库脚本

`sql` 目录存放 OA 系统的 MySQL 建表脚本和测试数据脚本，主要供 `system-service` 和 `business-service` 使用。

## 数据库

默认数据库名：

```sql
example_db
```

创建数据库：

```sql
CREATE DATABASE example_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE example_db;
```

## 脚本说明

| 文件 | 说明 | 所属服务 |
| --- | --- | --- |
| `ddl_User.sql` | 用户表 `sys_user` | `system-service` |
| `ddl_Role.sql` | 角色表 `sys_role` | `system-service` |
| `ddl_Permission.sql` | 权限表 `sys_permission` | `system-service` |
| `ddl_Menu.sql` | 菜单表 `sys_menu` 和菜单初始化数据 | `system-service` |
| `ddl_UserRole.sql` | 用户角色关系表 `sys_user_role` | `system-service` |
| `ddl_RolePermission.sql` | 角色权限关系表 `sys_role_permission` | `system-service` |
| `ddl_Product.sql` | 商品表 `sys_product` | `business-service` |
| `ddl_category.sql` | 商品分类表 `sys_category` | `business-service` |
| `ddl_Marketing.sql` | 营销活动表 `marketing` | `business-service` |
| `data_Marketing.sql` | 营销活动测试数据 | `business-service` |
| `test_data.sql` | 商品、订单等测试数据 | `business-service` |

## 推荐执行顺序

先执行建表脚本：

```text
ddl_User.sql
ddl_Role.sql
ddl_Permission.sql
ddl_Menu.sql
ddl_UserRole.sql
ddl_RolePermission.sql
ddl_category.sql
ddl_Product.sql
ddl_Marketing.sql
```

再执行数据脚本：

```text
data_Marketing.sql
test_data.sql
```

## 注意事项

- 执行前确认当前连接的数据库是 `example_db`。
- 部分脚本包含 `TRUNCATE` 或初始化数据，生产环境执行前需要先备份。
- 如果表已经存在，重复执行非 `IF NOT EXISTS` 的 DDL 可能报错。
- 数据库连接配置位于各服务的 `src/main/resources/application.yml`。

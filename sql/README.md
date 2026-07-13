# SQL 数据库脚本

`sql` 目录只保留当前 Docker 初始化需要的脚本。

## 文件说明

| 文件 | 说明 | 是否自动执行 |
| --- | --- | --- |
| `init.sql` | 业务库 `example_db` 的完整初始化脚本，包含本地真实业务表结构和数据 | 是 |
| `xxl_job.sql` | XXL-Job Admin 独立库表和默认数据 | 是 |

## Docker 初始化

项目根目录的 `docker-compose.yml` 只挂载下面两个脚本：

```yaml
./sql/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
./sql/xxl_job.sql:/docker-entrypoint-initdb.d/02-xxl-job.sql:ro
```

`init.sql` 当前包含：

```text
sys_user
sys_role
sys_permission
sys_menu
sys_user_role
sys_role_permission
sys_category
sys_product
sys_order
marketing
```

## 执行提醒

- `init.sql` 是业务库唯一初始化入口，不再维护拆分版 `ddl_*.sql` 或测试数据脚本。
- `init.sql` 包含 `DROP TABLE`，手动执行会重建对应业务表。
- Docker MySQL 已有数据卷时，`/docker-entrypoint-initdb.d/` 里的脚本不会重复执行；只有空数据卷首次启动才会执行。
- 如需重新初始化 Docker MySQL，需要先确认可以删除当前数据卷。

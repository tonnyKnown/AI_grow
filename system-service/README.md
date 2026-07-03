# System Service 系统服务

`system-service` 是 OA 系统的权限与基础管理服务，负责登录认证、用户管理、角色管理、权限管理、菜单管理和系统看板基础数据。

## 核心功能

| 功能 | 说明 |
| --- | --- |
| 登录认证 | 用户登录、登出、当前用户信息查询 |
| 用户管理 | 用户分页、详情、新增、编辑、删除、分配角色 |
| 角色管理 | 角色分页、详情、新增、编辑、删除、分配权限 |
| 权限管理 | 权限分页、权限树、菜单权限、新增、编辑、删除 |
| 菜单管理 | 菜单列表、菜单树、按角色查询菜单、新增、编辑、删除 |
| 系统看板 | 查询系统侧统计数据 |

## 技术栈

- Java 17
- Spring Boot 3.4.1
- Spring Web
- Spring Security
- Spring Cloud Alibaba Nacos Discovery
- Spring Cloud OpenFeign
- MyBatis
- MySQL
- Redis
- JJWT

## 服务信息

| 项目 | 值 |
| --- | --- |
| 服务名 | `system-service` |
| 端口 | `8081` |
| 注册中心 | Nacos |
| 数据库 | MySQL `example_db` |
| 配置文件 | `src/main/resources/application.yml` |
| 启动类 | `com.oa.system.SystemServiceApplication` |

## 主要接口

经网关访问时，前缀为 `/api/system`；服务内部接口前缀为 `/system`。

| 能力 | 网关路径 |
| --- | --- |
| 登录 | `POST /api/system/auth/login` |
| 登出 | `POST /api/system/auth/logout` |
| 当前用户 | `GET /api/system/auth/current` |
| 用户管理 | `/api/system/users/**` |
| 角色管理 | `/api/system/roles/**` |
| 权限管理 | `/api/system/permissions/**` |
| 菜单管理 | `/api/system/menu/**` |
| 看板统计 | `GET /api/system/dashboard/stats` |

## 启动方式

在项目根目录执行：

```powershell
mvn -pl system-service spring-boot:run
```

构建后运行：

```powershell
mvn -pl system-service clean package
java -jar system-service/target/system-service-1.0.0.jar
```

## 依赖服务

启动前需要保证：

- MySQL 已创建并初始化 `example_db`
- Redis 已启动
- Nacos 已启动
- `application.yml` 中数据库账号、Redis 地址、Nacos 地址正确

## 数据库脚本

相关脚本位于根目录 `sql/`：

```text
sql/ddl_User.sql
sql/ddl_Role.sql
sql/ddl_Permission.sql
sql/ddl_Menu.sql
sql/ddl_UserRole.sql
sql/ddl_RolePermission.sql
sql/test_data.sql
```

## 配置注意事项

- `jwt.secret` 需要和 `gateway` 保持一致。
- 数据库密码、JWT 密钥、Nacos 密码不建议提交真实生产值。
- MyBatis XML 映射文件位于 `src/main/resources/mapper/`。

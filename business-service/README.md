# Business Service 业务服务

`business-service` 是 OA 系统的业务管理服务，负责商品、订单、营销活动、业务看板、聊天会话、Agent 订单查询和定时任务管理。

## 核心功能

| 功能 | 说明 |
| --- | --- |
| 商品管理 | 商品分页、详情、新增、编辑、删除、库存调整 |
| 订单管理 | 订单分页、详情、新增、编辑、删除 |
| 营销管理 | 营销活动分页、详情、新增、编辑、删除、营销类型 |
| 业务看板 | 查询业务统计数据 |
| 智能聊天 | 聊天发送、机器人问答、会话创建、历史查询、会话清理 |
| Agent 订单查询 | 面向智能体的订单查询、订单详情、订单数量统计 |
| 任务管理 | XXL-Job 任务列表、任务启停、触发、日志查询 |

## 技术栈

- Java 17
- Spring Boot 3.4.1
- Spring Web
- Spring AOP
- Spring Cloud Alibaba Nacos Discovery
- Spring Cloud OpenFeign
- MyBatis
- MySQL
- Redis
- XXL-Job
- JJWT

## 服务信息

| 项目 | 值 |
| --- | --- |
| 服务名 | `business-service` |
| 端口 | `8082` |
| 注册中心 | Nacos |
| 数据库 | MySQL `example_db` |
| 配置文件 | `src/main/resources/application.yml` |
| 启动类 | `com.oa.business.BusinessServiceApplication` |

## 主要接口

经网关访问时，前缀为 `/api/business`；服务内部接口前缀为 `/business`。

| 能力 | 网关路径 |
| --- | --- |
| 商品管理 | `/api/business/products/**` |
| 订单管理 | `/api/business/orders/**` |
| 营销管理 | `/api/business/marketing/**` |
| 业务看板 | `GET /api/business/dashboard/stats` |
| 智能聊天 | `/api/business/chat/**` |
| Agent 订单查询 | `/api/business/agent/orders/**` |
| 任务管理 | `/api/business/api/jobs/**` |

## 启动方式

在项目根目录执行：

```powershell
mvn -pl business-service spring-boot:run
```

构建后运行：

```powershell
mvn -pl business-service clean package
java -jar business-service/target/business-service-1.0.0.jar
```

## 依赖服务

启动前需要保证：

- MySQL 已创建并初始化 `example_db`
- Redis 已启动
- Nacos 已启动
- 如需定时任务能力，启动 XXL-Job Admin 并开启 `xxl.job` 配置
- 如需聊天能力，确保本地 LLM 或兼容 OpenAI 的服务地址可用

## 数据库脚本

相关脚本位于根目录 `sql/`：

```text
sql/ddl_Product.sql
sql/ddl_category.sql
sql/ddl_Marketing.sql
sql/data_Marketing.sql
sql/test_data.sql
```

## 配置注意事项

- `jwt.secret` 需要和 `gateway` 保持一致。
- `openai.api-url` 当前默认指向本地兼容 OpenAI 接口的服务。
- 微信支付、数据库密码、JWT 密钥、Nacos 密码不建议提交真实生产值。
- MyBatis XML 映射文件位于 `src/main/resources/mapper/`。

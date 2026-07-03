# Gateway 网关服务

`gateway` 是 OA 系统的统一 API 入口，基于 Spring Cloud Gateway 实现请求路由、JWT 校验、跨域处理和统一异常响应。

## 核心职责

| 能力 | 说明 |
| --- | --- |
| 统一入口 | 前端所有 `/api/**` 请求先进入网关 |
| 服务路由 | 根据路径转发到 `system-service`、`business-service`、`javachain` |
| JWT 鉴权 | 校验 `Authorization: Bearer <token>`，并向下游透传用户信息 |
| 白名单放行 | 登录、公开接口等路径不需要 Token |
| 跨域处理 | 通过 `CorsWebFilter` 支持前端开发环境访问 |
| 异常处理 | `GlobalExceptionFilter` 统一处理网关层异常 |

## 技术栈

- Java 17
- Spring Boot 3.4.1
- Spring Cloud Gateway
- Spring Cloud Alibaba Nacos Discovery
- JJWT

## 服务信息

| 项目 | 值 |
| --- | --- |
| 服务名 | `gateway` |
| 端口 | `8085` |
| 注册中心 | Nacos |
| 配置文件 | `src/main/resources/application.yml` |
| 启动类 | `com.oa.gateway.GatewayApplication` |

## 路由规则

| 前端请求 | 目标服务 | 转发规则 |
| --- | --- | --- |
| `/api/system/**` | `system-service` | 重写为 `/system/**` |
| `/api/business/**` | `business-service` | 去掉 `/api` 前缀 |
| `/api/javachain/**` | `javachain` | 重写为 `/api/**` |

示例：

```text
/api/system/auth/login        -> system-service /system/auth/login
/api/business/products        -> business-service /business/products
/api/javachain/chat/simple    -> javachain /api/chat/simple
```

## 启动方式

在项目根目录执行：

```powershell
mvn -pl gateway spring-boot:run
```

也可以先构建再运行：

```powershell
mvn -pl gateway clean package
java -jar gateway/target/gateway-1.0.0.jar
```

## 依赖服务

启动前需要保证：

- Nacos 已启动，默认地址 `127.0.0.1:8848`
- 下游服务已经注册到 Nacos
- `system-service`、`business-service`、`javachain` 的服务名和网关路由一致

## 配置注意事项

- `jwt.secret` 必须和签发 Token 的系统服务保持一致。
- `gateway.trusted-services` 属于服务间调用密钥，不建议提交真实生产值。
- 本地开发时前端 Vite 会将 `/api` 代理到 `http://localhost:8085`。

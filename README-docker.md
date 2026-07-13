# Docker Desktop 本地运行

这套配置用于在本机 Docker Desktop 中运行 OA 系统，包含 MySQL、Redis、Nacos、XXL-Job、网关、系统服务、业务服务、mysql-plugin、javachain 和前端。

## 1. 前置条件

- 已启动 Docker Desktop
- 本机已安装 JDK 17+ 和 Maven
- 本机需要能成功打包根工程、`javachain/target/javachain-1.0.0.jar` 和 `D:\my_mcp\mysql-plugin\target\mysql-plugin-1.0.0.jar`

如果 Docker Desktop 拉取 Docker Hub 镜像超时，可以复制镜像配置示例：

```powershell
Copy-Item .env.docker.example .env
```

然后打开 `.env`，把“国内镜像示例”里的配置取消注释，并注释掉上面的官方默认镜像。

## 2. 打包后端

在项目根目录执行：

```powershell
mvn -DskipTests package
```

再单独打包 `javachain`：

```powershell
cd javachain
mvn -DskipTests package
cd ..
```

再打包 MCP MySQL 插件：

```powershell
cd D:\my_mcp\mysql-plugin
mvn -DskipTests package
cd D:\vue\java-backend
```

## 3. 启动服务

```powershell
docker compose up -d --build
```

访问地址：

```text
前端：http://localhost:5173
网关：http://localhost:8085
Nacos 控制台：http://localhost:8080
XXL-Job 控制台：http://localhost:8088/xxl-job-admin，账号 admin / 123456
MySQL：localhost:3307，root / your_actual_password
Redis：localhost:6380
mysql-plugin：http://localhost:8083
javachain：http://localhost:8087
```

## 4. AI 环境变量

如果需要调用 DeepSeek 或 DashScope，可以在启动前配置环境变量：

```powershell
$env:DEEPSEEK_API_KEY="你的 key"
$env:DASHSCOPE_API_KEY="你的 key"
docker compose up -d --build
```

## 5. 查看日志

```powershell
docker compose logs -f gateway
docker compose logs -f system-service
docker compose logs -f business-service
docker compose logs -f xxl-job-admin
docker compose logs -f mysql-plugin
```

## 6. 停止服务

```powershell
docker compose down
```

如果要连 MySQL 数据一起删除，重新初始化数据库：

```powershell
docker compose down -v
```

## 7. 说明

- 容器内部 MySQL 端口是 `3306`，映射到本机 `3307`，避免和本机 MySQL 冲突。
- 容器内部 Redis 端口是 `6379`，映射到本机 `6380`，避免和本机 Redis 冲突。
- XXL-Job Admin 容器内部端口是 `8080`，映射到本机 `8088`，避免和 Nacos 控制台冲突。
- `business-service` 会在 Docker 环境中注册到 `xxl-job-admin`，执行器端口是 `9999`。
- `mysql-plugin` 是 MCP MySQL 工具服务，容器内连接 `mysql:3306`，并注册到 `nacos:8848`。
- 前端容器使用 Nginx，将 `/api` 反向代理到 `gateway:8085`。
- `javachain` 默认启动，并依赖 `mysql-plugin`；如果不配置 AI Key，涉及外部大模型调用的功能可能不可用。
- 当前配置支持通过 `.env` 覆盖基础镜像，方便在 Docker Hub 网络不可用时切换镜像源。

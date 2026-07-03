# JavaChain AI 服务

`javachain` 是 OA 系统中的 AI 能力服务，基于 Spring Boot 和 LangChain4j 构建，提供大模型对话、RAG 知识库问答、文件向量化、Skills 技能执行、MCP 工具调用和 ReAct Agent。

## 核心功能

| 功能 | 说明 |
| --- | --- |
| 大模型对话 | 支持简单问答、带历史会话问答、自动问答 |
| RAG 知识库 | 文档加载、知识库检索问答、知识库统计、清空 |
| 文件处理 | 文件列表、单文件向量化、批量向量化、上传和文本提取 |
| ReAct Agent | 多步骤推理、工具调用、任务状态查询、人工确认 |
| Skills | 技能列表、技能重载、指定技能执行、自动识别技能 |
| MCP | MCP 服务管理、工具列表、工具执行、插件加载 |
| 插件治理 | 插件启用、禁用、版本检查、版本比较 |

## 技术栈

- Java 21
- Spring Boot 3.3.1
- Spring Web / WebFlux
- Spring Cloud Alibaba Nacos Discovery
- LangChain4j 0.35.0
- DeepSeek / Ollama / DashScope
- MySQL Connector
- Redis
- PDFBox
- HanLP
- Resilience4j

## 服务信息

| 项目 | 值 |
| --- | --- |
| 服务名 | `javachain` |
| 端口 | `8087` |
| 注册中心 | Nacos |
| 配置文件 | `src/main/resources/application.yml`、`src/main/resources/bootstrap.yml` |
| 启动类 | `com.example.javachain.JavachainApplication` |

`bootstrap.yml` 中的服务名为 `javachain`，需要和网关 `lb://javachain` 路由保持一致。

## 项目结构

```text
javachain
├── src/main/java/com/example/javachain
│   ├── agent           # ReAct Agent 执行逻辑
│   ├── common          # 统一返回结构
│   ├── config          # LLM、Redis、MCP、Agent 配置
│   ├── controller      # Chat、Agent、Document 接口
│   ├── model           # 会话消息模型
│   ├── plugin          # 插件元信息
│   ├── security        # 工具参数校验与安全审计
│   ├── service         # 对话、RAG、MCP、文件向量化等服务
│   └── skill           # 技能定义、解析、执行
├── src/main/resources
│   ├── application.yml
│   ├── bootstrap.yml
│   ├── static
│   └── file
└── pom.xml
```

## LLM 配置

服务支持通过 `javachain.llm.provider` 切换模型提供方：

| Provider | 说明 |
| --- | --- |
| `deepseek` | 使用 DeepSeek 云端 API |
| `ollama` | 使用本地 Ollama 模型 |

建议使用环境变量配置敏感信息：

```powershell
$env:JAVACHAIN_LLM_PROVIDER="deepseek"
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
$env:DEEPSEEK_BASE_URL="https://api.deepseek.com/v1"
$env:DASHSCOPE_API_KEY="your_dashscope_api_key"
```

使用 Ollama 时：

```powershell
$env:JAVACHAIN_LLM_PROVIDER="ollama"
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_MODEL="deepseek-r1:7b"
```

## 主要接口

经网关访问时，前缀为 `/api/javachain`；服务内部接口前缀为 `/api`。

### Chat 接口

| 能力 | 网关路径 |
| --- | --- |
| 创建会话 | `POST /api/javachain/chat/session/create` |
| 清空会话 | `POST /api/javachain/chat/session/{sessionId}/clear` |
| 简单对话 | `POST /api/javachain/chat/simple` |
| 带历史对话 | `POST /api/javachain/chat/simple/with-history` |
| 自动问答 | `POST /api/javachain/chat/auto` |
| RAG 问答 | `POST /api/javachain/chat/rag` |
| 加载文档 | `POST /api/javachain/chat/rag/load` |
| RAG 统计 | `GET /api/javachain/chat/rag/stats` |
| 清空知识库 | `POST /api/javachain/chat/rag/clear` |
| 文件列表 | `GET /api/javachain/chat/files/list` |
| 批量向量化 | `GET /api/javachain/chat/files/vectorize` |
| 单文件向量化 | `POST /api/javachain/chat/files/vectorize/single` |
| MCP 工具列表 | `GET /api/javachain/chat/mcp/tools` |
| MCP 工具执行 | `POST /api/javachain/chat/mcp/execute` |
| 天气查询 | `GET /api/javachain/chat/weather` |

### Agent 接口

| 能力 | 网关路径 |
| --- | --- |
| 启动 ReAct 任务 | `POST /api/javachain/agent/react/start` |
| 查询任务状态 | `GET /api/javachain/agent/react/status/{jobId}` |
| 确认任务 | `POST /api/javachain/agent/react/confirm` |
| 同步 ReAct 执行 | `POST /api/javachain/agent/react` |
| 快速执行 | `POST /api/javachain/agent/react/quick` |
| 步骤说明 | `GET /api/javachain/agent/steps` |
| SSE 流式执行 | `POST /api/javachain/agent/react/stream` |

### Document 接口

| 能力 | 网关路径 |
| --- | --- |
| 上传文档 | `POST /api/javachain/document/upload` |
| 加载文档 | `POST /api/javachain/document/load` |
| 提取文本 | `POST /api/javachain/document/extract` |
| 支持格式 | `GET /api/javachain/document/supported-formats` |

## 启动方式

进入 `javachain` 目录：

```powershell
cd javachain
mvn spring-boot:run
```

构建后运行：

```powershell
cd javachain
mvn clean package
java -jar target/javachain-1.0.0.jar
```

## 依赖服务

启动前需要保证：

- JDK 21 已配置
- Nacos 已启动
- Redis 已启动
- 如使用 MySQL MCP 工具，MySQL 连接信息正确
- 如使用 DeepSeek 或 DashScope，环境变量中已配置对应 API Key
- 如使用 Ollama，本地 Ollama 服务已启动并拉取指定模型

## 配置注意事项

- 不要提交真实 `DEEPSEEK_API_KEY`、`DASHSCOPE_API_KEY`、数据库密码和 Nacos 密码。
- `vector-store.type` 当前配置为 `memory`，服务重启后内存向量数据会丢失。
- `mcp.auto-load` 当前为 `false`，插件不会自动加载。
- 网关路由依赖服务名 `javachain`，不要随意修改 `bootstrap.yml` 中的 `spring.application.name`。

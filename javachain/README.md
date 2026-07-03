
# JavaChain - Spring Boot + LangChain Integration

一个完整的 Java Spring Boot 项目，集成了 LangChain、RAG、Skills 和 MCP，默认使用 DeepSeek 大模型。

## 技术栈

- Java 21
- Spring Boot 3.2.5
- LangChain4j 0.32.0
- DeepSeek 大模型

## 项目结构

```
javachain/
├── src/main/java/com/example/javachain/
│   ├── JavachainApplication.java    # 启动类
│   ├── config/
│   │   └── DeepSeekConfig.java      # DeepSeek 模型配置
│   ├── controller/
│   │   └── ChatController.java      # 聊天接口
│   └── service/
│       ├── ChatService.java         # 聊天服务
│       ├── RagService.java          # RAG 服务
│       ├── SkillService.java        # 技能服务
│       └── McpService.java          # MCP 服务
├── src/main/resources/
│   └── application.yml              # 应用配置
└── pom.xml                          # Maven 依赖
```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+

### 2. 配置 DeepSeek API Key

设置环境变量：

```bash
# Linux/Mac
export DEEPSEEK_API_KEY=your-api-key

# Windows
set DEEPSEEK_API_KEY=your-api-key
```

或者修改 `application.yml` 中的配置：

```yaml
deepseek:
  api:
    key: your-api-key-here
```

### 3. 构建项目

```bash
mvn clean compile -DskipTests
```

### 4. 运行项目

```bash
mvn spring-boot:run
```

## API 接口

### 简单对话

```bash
curl -X POST http://localhost:8080/api/chat/simple \
  -H "Content-Type: application/json" \
  -d '{"message": "你好"}'
```

### RAG 检索增强对话

```bash
# 先加载文档到知识库
curl -X POST http://localhost:8080/api/chat/rag/load \
  -H "Content-Type: application/json" \
  -d '{"title": "产品文档", "text": "我们的产品是一款企业级OA系统，支持请假审批、订单管理、商品管理等功能。"}'

# 进行检索问答
curl -X POST http://localhost:8080/api/chat/rag \
  -H "Content-Type: application/json" \
  -d '{"question": "你们的产品支持哪些功能？"}'
```

### MCP 工具调用

```bash
curl -X POST http://localhost:8080/api/chat/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "calculate",
    "arguments": {"expression": "100 + 200"}
  }'
```

### 获取可用技能

```bash
curl http://localhost:8080/api/chat/skills
```

### 获取可用 MCP 工具

```bash
curl http://localhost:8080/api/chat/mcp/tools
```

## 核心功能

### 1. 大模型对话
- 基于 DeepSeek 大模型
- 支持多轮对话

### 2. RAG (检索增强生成)
- 内存知识库存储
- 上下文增强回答

### 3. Skills (技能系统)
- `getCurrentTime()` - 获取当前时间
- `generateRandomNumber(min, max)` - 生成随机数
- `reverseString(input)` - 反转字符串
- `addNumbers(a, b)` - 加法运算

### 4. MCP (模型上下文协议)
- 外部工具调用
- 搜索、计算、天气等模拟工具

## 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `deepseek.api.key` | DeepSeek API Key | - |
| `deepseek.api.base-url` | API 地址 | https://api.deepseek.com/v1 |
| `deepseek.api.chat-model` | 模型名称 | deepseek-chat |

## License

Apache 2.0

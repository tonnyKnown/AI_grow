# API 接口文档

> 项目名称：OA 智能办公管理系统
> 文档版本：v1.0
> 最后更新：2026-10-04
> 适用对象：前后端研发、测试、第三方对接方

---

## 1. 通用约定

### 1.1 服务地址

| 环境 | 网关地址 | 说明 |
| --- | --- | --- |
| 本地开发 | `http://localhost:8085` | 前端 Vite 自动代理 `/api` 到此地址 |
| Docker | `http://localhost:8085` | 前端 Nginx 将 `/api` 反代到 gateway |
| 前端访问入口 | `http://localhost:5173` | 浏览器访问地址 |

**所有前端请求统一走网关**，路径前缀为 `/api`。

### 1.2 鉴权

除白名单接口外，所有请求必须携带 JWT：

```http
Authorization: Bearer <token>
```

**白名单（无需 Token）**：

- `POST /api/system/auth/login`
- `POST /api/system/auth/logout`
- `/api/python/**`

**网关行为**：校验通过后，向请求头注入以下字段供下游服务使用：

| 请求头 | 说明 |
| --- | --- |
| `userId` | 当前用户 ID |
| `X-Username` | 当前用户名 |

**服务间调用**：携带 `Service-Key: <密钥>` 可跳过 JWT 校验，网关注入 `userId=0`、`X-Username=python-service`、`X-Service-Call=true`。

### 1.3 路径重写规则

网关会对路径做重写，**调用方只关注 `/api/xxx` 前缀即可**：

| 前端请求 | 实际转发到 |
| --- | --- |
| `/api/system/**` | `system-service` 的 `/system/**` |
| `/api/business/**` | `business-service` 的 `/business/**` |
| `/api/javachain/**` | `javachain` 的 `/api/**` |

### 1.4 统一响应结构

**system-service / business-service**（`Result<T>`）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**javachain**（`ApiResult<T>`，多一个 `success` 字段）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "success": true
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | int | 200 成功；401 未认证；500 服务端错误 |
| `message` | string | 提示信息 |
| `data` | any | 业务数据 |
| `success` | boolean | 仅 javachain 返回 |

### 1.5 分页结构

列表接口统一返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `pageNum` | 1 | 页码，从 1 开始 |
| `pageSize` | 10 | 每页条数 |

### 1.6 状态码

| 状态码 | 含义 | 触发场景 |
| --- | --- | --- |
| 200 | 成功 | — |
| 401 | 未认证 | 缺少 Token、Token 无效或已登出 |
| 403 | 无权限 | 前端拦截提示 |
| 500 | 服务端错误 | 业务异常 |
| 502 / 503 | 服务不可用 | 后端未启动，前端跳转维护页 |

---

## 2. 系统服务接口（system-service）

网关前缀：`/api/system`

### 2.1 认证

#### 2.1.1 登录

```http
POST /api/system/auth/login
Content-Type: application/json
```

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 登录账号 |
| `password` | string | 是 | 登录密码（明文传输，由 HTTPS 保障） |

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "张三",
    "roles": ["admin"]
  }
}
```

**异常**：

| code | message |
| --- | --- |
| 401 | 账号或密码错误 |

#### 2.1.2 登出

```http
POST /api/system/auth/logout
Authorization: Bearer <token>
```

**说明**：将 Token 加入 Redis 黑名单，使其立即失效。

**响应**：`{ "code": 200, "message": "退出成功", "data": null }`

#### 2.1.3 获取当前用户

```http
GET /api/system/auth/current
Authorization: Bearer <token>
```

**说明**：从网关透传的 `X-Username` 头读取用户名。

**响应 data**：`UserResponse`，含用户基本信息与角色列表。

| code | 场景 |
| --- | --- |
| 401 | 未认证（缺少 X-Username 头） |

#### 2.1.4 获取当前用户菜单

```http
GET /api/system/auth/menu
Authorization: Bearer <token>
```

**说明**：根据当前用户角色返回可见菜单列表。

**响应**：`Menu` 数组。未认证或用户无角色时返回空数组。

### 2.2 用户管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/system/users` | 用户分页列表 |
| GET | `/api/system/users/{id}` | 用户详情 |
| GET | `/api/system/users/username/{username}` | 按用户名查询 |
| GET | `/api/system/users/getUserByIName?userName=` | 按真实姓名查询（分页） |
| POST | `/api/system/users` | 新增用户 |
| PUT | `/api/system/users` | 更新用户 |
| DELETE | `/api/system/users/{id}` | 删除用户 |
| POST | `/api/system/users/{userId}/roles` | 分配角色 |

**新增/更新用户请求体**（`UserRequest`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 用户 ID（更新时必填） |
| `username` | string | 登录账号 |
| `password` | string | 密码（新增时必填） |
| `email` | string | 邮箱 |
| `phone` | string | 手机号 |
| `realName` | string | 真实姓名 |
| `avatar` | string | 头像地址 |
| `status` | int | 0 禁用、1 启用 |
| `remark` | string | 备注 |

**分配角色请求体**：

```json
[1, 2]
```

> 注意：创建/更新接口从请求头 `userId` 获取操作人，缺失时默认 `1`。

### 2.3 角色管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/system/roles` | 角色分页列表 |
| GET | `/api/system/roles/all` | 全部角色（不分页，用于下拉选择） |
| GET | `/api/system/roles/{id}` | 角色详情 |
| GET | `/api/system/roles/{id}/permissions` | 角色已分配的权限 ID 列表 |
| GET | `/api/system/roles/permissions/tree` | 权限树 |
| POST | `/api/system/roles` | 新增角色 |
| PUT | `/api/system/roles` | 更新角色 |
| DELETE | `/api/system/roles/{id}` | 删除角色 |
| POST | `/api/system/roles/{id}/permissions` | 分配权限 |

**角色对象**（`Role`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 角色 ID |
| `roleName` | string | 角色名称 |
| `roleKey` | string | 角色标识（如 admin、user） |
| `description` | string | 描述 |
| `status` | int | 0 禁用、1 启用 |
| `remark` | string | 备注 |

**分配权限请求体**：

```json
[6, 7]
```

> **权限校验**：写操作（新增/更新/删除/分配权限）会校验 `role:manage` 权限，无权限时返回 `{ "code": 500, "message": "无权XX角色，请联系管理员" }`。

### 2.4 权限管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/system/permissions` | 权限分页列表 |
| GET | `/api/system/permissions/all` | 全部权限 |
| GET | `/api/system/permissions/menu` | 权限树（菜单权限） |
| GET | `/api/system/permissions/{id}` | 权限详情 |
| POST | `/api/system/permissions` | 新增权限 |
| PUT | `/api/system/permissions` | 更新权限 |
| DELETE | `/api/system/permissions/{id}` | 删除权限 |

**权限对象**（`Permission`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 权限 ID |
| `permissionName` | string | 权限名称 |
| `permissionKey` | string | 权限标识（如 `role:manage`） |
| `resourceType` | string | 资源类型 |
| `path` | string | 资源路径 |
| `component` | string | 前端组件 |
| `parentId` | long | 父级权限 ID |
| `orderNum` | int | 排序 |
| `status` | int | 0 禁用、1 启用 |

> **权限校验**：写操作校验 `permission:manage` 权限。

### 2.5 菜单管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/system/menu/list` | 全部菜单（平铺） |
| GET | `/api/system/menu/tree` | 菜单树 |
| GET | `/api/system/menu/roles?roles=` | 按角色查询菜单 |
| GET | `/api/system/menu/{id}` | 菜单详情 |
| POST | `/api/system/menu` | 新增菜单 |
| PUT | `/api/system/menu/{id}` | 更新菜单 |
| DELETE | `/api/system/menu/{id}` | 删除菜单 |

**菜单对象**（`Menu`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 菜单 ID |
| `menuName` | string | 菜单名称 |
| `path` | string | 路由路径 |
| `component` | string | 前端组件路径 |
| `icon` | string | 图标 |
| `parentId` | long | 父级菜单 ID |
| `orderNum` | int | 排序 |
| `roleKeys` | string | 可访问角色标识，逗号分隔 |
| `status` | int | 0 禁用、1 启用 |

**按角色查询示例**：

```http
GET /api/system/menu/roles?roles=admin,user
```

### 2.6 系统看板

```http
GET /api/system/dashboard/stats
```

**响应 data**（`DashboardStats`）：系统侧统计数据（用户数等）。

> 该接口也被 business-service 通过 OpenFeign 调用以聚合数据。

---

## 3. 业务服务接口（business-service）

网关前缀：`/api/business`

### 3.1 商品管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/business/products` | 商品分页列表 |
| GET | `/api/business/products/{id}` | 商品详情 |
| POST | `/api/business/products` | 新增商品 |
| PUT | `/api/business/products` | 更新商品 |
| DELETE | `/api/business/products/{id}` | 删除商品 |

**商品请求体**（`ProductRequest`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 商品 ID（更新时必填） |
| `productName` | string | 商品名称 |
| `productCode` | string | 商品编码 |
| `category` | string | 商品分类 |
| `price` | decimal | 价格 |
| `stock` | int | 库存 |
| `description` | string | 描述 |
| `imageUrl` | string | 图片地址 |
| `status` | int | 0 禁用、1 启用 |

**示例**：

```json
{
  "productName": "青岛啤酒",
  "productCode": "1231231",
  "category": "酒类",
  "price": 12.00,
  "stock": 1215,
  "description": "",
  "status": 1
}
```

### 3.2 订单管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/business/orders` | 订单分页列表 |
| GET | `/api/business/orders/{id}` | 订单详情 |
| POST | `/api/business/orders` | 新增订单 |
| PUT | `/api/business/orders` | 更新订单 |
| DELETE | `/api/business/orders/{id}` | 删除订单 |

**订单对象**（`Order`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 订单 ID |
| `orderNo` | string | 订单编号 |
| `userId` | long | 下单用户 ID |
| `productId` | long | 商品 ID |
| `productName` | string | 商品名称快照 |
| `quantity` | int | 购买数量 |
| `unitPrice` | decimal | 单价 |
| `totalAmount` | decimal | 总金额 |
| `status` | int | 订单状态（见下表） |
| `shippingAddress` | string | 收货地址 |
| `receiverName` | string | 收货人 |
| `receiverPhone` | string | 收货人手机号 |
| `remark` | string | 备注 |

**订单状态取值**（依据现有数据推断）：

| 值 | 含义 |
| --- | --- |
| 1 | 待处理 |
| 2 | 处理中 |
| 3 | 已完成 |
| 4 | 已发货 |
| 7 | 异常 / 取消 |
| 8 | 已收货 |

> ⚠️ 订单状态在代码中未定义枚举，取值来自数据库实际数据，**建议后续补充状态枚举统一定义**。

### 3.3 Agent 订单查询接口

> 这组接口**专供 AI 智能体调用**，便于智能体查询真实订单数据。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/business/agent/orders` | 查询订单列表 |
| GET | `/api/business/agent/orders/{id}` | 订单详情 |
| GET | `/api/business/agent/orders/count` | 订单数量统计 |

**查询参数（列表）**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | long | 否 | 用户 ID |
| `orderNo` | string | 否 | 订单编号 |
| `status` | int | 否 | 订单状态 |
| `pageNum` | int | 否 | 默认 1 |
| `pageSize` | int | 否 | 默认 10 |

**数量统计响应**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 12,
    "status": "all"
  }
}
```

> 该组接口均带 `@ApiLog` 注解，会记录调用日志。

### 3.4 物流管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/business/express/order/{orderId}` | 按订单 ID 查询物流 |
| GET | `/api/business/express/list` | 物流分页列表 |
| GET | `/api/business/express/{id}` | 物流详情（经 `/query/{id}` 间接使用） |
| GET | `/api/business/express/query/{id}` | 查询实时轨迹 |
| POST | `/api/business/express` | 创建物流 |
| PUT | `/api/business/express` | 更新物流 |
| PUT | `/api/business/express/tracking` | 更新物流轨迹 |
| DELETE | `/api/business/express/{id}` | 删除物流 |

**列表查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | int | 否 | 默认 1 |
| `pageSize` | int | 否 | 默认 10 |
| `orderNo` | string | 否 | 订单编号 |
| `expressCompany` | string | 否 | 快递公司 |
| `status` | int | 否 | 物流状态 |

**物流对象**（`Express`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 物流 ID |
| `orderId` | long | 订单 ID |
| `orderNo` | string | 订单编号 |
| `expressCompany` | string | 快递公司 |
| `expressNo` | string | 运单号 |
| `status` | int | 0 已揽收、1 运输中、2 派送中、3 已签收、4 异常 |
| `trackingNodes` | json | 轨迹节点数组 |
| `senderName` | string | 发件人 |
| `senderPhone` | string | 发件人电话 |
| `senderAddress` | string | 发件地址 |

**更新轨迹请求体**：

```json
{
  "id": 1,
  "trackingNodes": "[{\"time\":\"2026-05-10 10:00:00\",\"status\":\"运输中\",\"desc\":\"已到达广州分拨中心\"}]"
}
```

**实时查询响应**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "trackingNodes": [],
    "status": 1,
    "expressCompany": "SF",
    "expressNo": "SF1234567890"
  }
}
```

**说明**：若该快递公司未接入查询服务，返回 `data: null` 且 `message` 为"当前快递公司暂不支持实时查询"。

### 3.5 营销管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/business/marketing` | 活动分页列表 |
| GET | `/api/business/marketing/{id}` | 活动详情 |
| GET | `/api/business/marketing/types` | 营销类型字典 |
| POST | `/api/business/marketing` | 新增活动 |
| PUT | `/api/business/marketing` | 更新活动 |
| DELETE | `/api/business/marketing/{id}` | 删除活动 |

**营销类型字典响应**：

```json
{
  "code": 200,
  "data": [
    { "code": "discount", "name": "折扣" },
    { "code": "flash", "name": "秒杀" },
    { "code": "group", "name": "团购" },
    { "code": "coupon", "name": "优惠券" }
  ]
}
```

**活动请求体**（`MarketingRequest`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 活动 ID |
| `name` | string | 活动名称 |
| `type` | string | 活动类型（discount / flash / group / coupon） |
| `description` | string | 活动描述 |
| `startTime` | datetime | 开始时间 |
| `endTime` | datetime | 结束时间 |
| `status` | int | 0 禁用、1 启用 |
| `rules` | string | 规则 JSON |
| `productIds` | string | 参与商品范围 |

**规则 JSON 示例**：

```json
{ "discount": 0.85 }
{ "min_members": 3, "discount": 0.7 }
{ "coupon_value": 50 }
{ "max_quantity": 100 }
```

### 3.6 业务看板

```http
GET /api/business/dashboard/stats
```

**响应 data**（`DashboardStats`）：业务侧统计（商品数、订单数、营销活动数等）。

### 3.7 定时任务

网关前缀为 `/api/business/api/jobs`（注意路径中含两段 `api`）。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/business/api/jobs/list` | 任务列表 |
| GET | `/api/business/api/jobs/{id}` | 任务详情 |
| POST | `/api/business/api/jobs` | 新增任务 |
| PUT | `/api/business/api/jobs/{id}` | 更新任务 |
| DELETE | `/api/business/api/jobs/{id}` | 删除任务 |
| POST | `/api/business/api/jobs/{id}/start` | 启动任务 |
| POST | `/api/business/api/jobs/{id}/stop` | 停止任务 |
| POST | `/api/business/api/jobs/{id}/trigger` | 立即触发一次 |
| GET | `/api/business/api/jobs/handlers` | 可用处理器列表 |
| GET | `/api/business/api/jobs/logs/{jobId}` | 任务执行日志 |

**任务对象**（`JobTaskDto`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 任务 ID |
| `jobName` | string | 任务名称 |
| `jobDesc` | string | 任务描述 |
| `jobHandler` | string | 处理器名称 |
| `cron` | string | Cron 表达式 |
| `jobParam` | string | 任务参数 |
| `status` | int | 0 停止、1 运行 |
| `shardingCount` | int | 分片数 |
| `creator` | string | 创建人 |
| `createTime` | date | 创建时间 |
| `runTimes` | int | 执行次数 |
| `successTimes` | int | 成功次数 |
| `failTimes` | int | 失败次数 |

> ⚠️ **重要**：该组接口当前返回的是**硬编码演示数据**，尚未真正对接 XXL-Job Admin。

---

## 4. AI 服务接口（javachain）

网关前缀：`/api/javachain`

### 4.1 会话管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/javachain/chat/session/create` | 创建会话 |
| DELETE | `/api/javachain/chat/session/{sessionId}` | 删除会话 |
| POST | `/api/javachain/chat/session/{sessionId}/clear` | 清空会话历史 |
| GET | `/api/javachain/chat/session/{sessionId}/history` | 获取会话历史 |

**创建会话响应**：

```json
{
  "code": 200,
  "data": { "sessionId": "a1b2c3d4-..." },
  "success": true
}
```

**获取历史响应 data**：`ChatMessage` 数组，通常含 `role`（user/assistant）与 `content`。

### 4.2 对话

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/javachain/chat/simple` | 简单对话（无历史） |
| POST | `/api/javachain/chat/simple/with-history` | 带历史的多轮对话 |
| POST | `/api/javachain/chat/auto` | 自动工具问答 |

**简单对话请求体**：

```json
{ "message": "你好" }
```

**带历史对话请求体**：

```json
{
  "sessionId": "a1b2c3d4-...",
  "message": "我的订单有哪些"
}
```

> `sessionId` 为空时会自动创建新会话，但**响应不会返回新生成的 sessionId**，若需多轮对话请先调用创建会话接口。

**自动工具问答请求体**：

```json
{ "question": "查询用户 1 的订单" }
```

**响应**：`data` 为模型返回的文本字符串。

### 4.3 知识库（RAG）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/javachain/chat/rag` | RAG 检索问答 |
| POST | `/api/javachain/chat/rag/load` | 加载文本到知识库 |
| GET | `/api/javachain/chat/rag/stats` | 知识库统计 |
| POST | `/api/javachain/chat/rag/clear` | 清空知识库 |

**RAG 问答请求体**：

```json
{ "question": "公司的报销流程是什么" }
```

**响应 data 示例**：

```text
[信息来源：知识库]

根据《员工手册》第 3 章，报销流程为：先提交申请…… [文档1]

---
**引用来源：**
- 员工手册 (相关度: 8.50)
```

**加载文本请求体**：

```json
{
  "text": "文档正文内容……",
  "title": "员工手册"
}
```

**统计响应**：形如 `知识库统计：文档总数 = 12` 的字符串。

### 4.4 文件向量化

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/javachain/chat/files/list?directory=` | 文件列表 |
| GET | `/api/javachain/chat/files/vectorize` | 批量向量化（目录固定） |
| POST | `/api/javachain/chat/files/vectorize/single` | 单文件向量化 |

**单文件向量化请求体**：

```json
{ "filePath": "D:\\docs\\手册.pdf" }
```

> ⚠️ 批量向量化接口的目录在代码中**硬编码**（`D:\agentDemo\javachain\src\main\resources\file\`），迁移环境时需修改。

### 4.5 MCP 工具

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/javachain/chat/mcp/tools` | 获取可用工具列表 |
| POST | `/api/javachain/chat/mcp/execute` | 执行指定工具 |

**工具执行请求体**：

```json
{
  "toolName": "getCurrentTime",
  "arguments": {}
}
```

**另有**：`GET /api/javachain/chat/weather?city=深圳` — 通过 Nacos A2A 协议调用外部天气 Agent。

### 4.6 ReAct Agent

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/javachain/agent/react/start` | 启动异步任务 |
| GET | `/api/javachain/agent/react/status/{jobId}` | 查询任务状态 |
| POST | `/api/javachain/agent/react/confirm` | 确认/取消待确认操作 |
| POST | `/api/javachain/agent/react` | 同步执行 |
| POST | `/api/javachain/agent/react/quick` | 快速执行（简化输出） |
| GET | `/api/javachain/agent/steps` | 推理引擎说明 |
| POST | `/api/javachain/agent/react/stream` | SSE 流式执行 |

#### 4.6.1 启动异步任务

```http
POST /api/javachain/agent/react/start
Content-Type: application/json
```

```json
{ "question": "帮我查一下订单 ORD1778360011790A9F4DADF 的物流状态" }
```

**响应**：

```json
{
  "code": 200,
  "data": { "jobId": "uuid-...", "status": "started" },
  "success": true
}
```

#### 4.6.2 查询任务状态

```http
GET /api/javachain/agent/react/status/{jobId}
```

**响应 data**（进行中）：

```json
{
  "jobId": "uuid-...",
  "status": "running",
  "question": "帮我查一下订单……"
}
```

**响应 data**（待确认）：

```json
{
  "jobId": "uuid-...",
  "status": "pending_confirmation",
  "toolName": "filesystem_delete",
  "description": "工具：filesystem_delete\n参数：{...}",
  "actionInput": "{...}"
}
```

**响应 data**（已完成）：

```json
{
  "jobId": "uuid-...",
  "status": "completed",
  "answer": "订单当前处于运输中……",
  "duration": 3200,
  "history": ["步骤 1: ...", "步骤 2: ..."]
}
```

| status 取值 | 含义 |
| --- | --- |
| `running` | 执行中 |
| `pending_confirmation` | 等待用户确认 |
| `completed` | 已完成 |
| `cancelled` | 已取消 |
| `failed` | 失败 |

| code | 场景 |
| --- | --- |
| 404 | 任务不存在 |

#### 4.6.3 确认操作

```http
POST /api/javachain/agent/react/confirm
```

```json
{ "jobId": "uuid-...", "confirm": true }
```

**响应**：`{ "jobId": "...", "status": "confirmed" }` 或 `{ "status": "cancelled" }`。

| code | 场景 |
| --- | --- |
| 400 | 任务不在待确认状态 |
| 404 | 任务不存在 |

#### 4.6.4 同步执行

```http
POST /api/javachain/agent/react
```

```json
{ "question": "现在几点了" }
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "question": "现在几点了",
    "answer": "现在是 2026-10-04 12:50:00",
    "duration": 1500,
    "steps": 2,
    "status": "completed"
  }
}
```

若为危险操作，`status` 为 `pending_confirmation`，并返回 `jobId`、`toolName`、`description`。

#### 4.6.5 SSE 流式执行

```http
POST /api/javachain/agent/react/stream
Accept: text/event-stream
```

**请求体**：`{ "question": "..." }`

**事件流**：

| 事件名 | 数据 |
| --- | --- |
| `step` | 单步详情：`stepNumber`、`thought`、`action`、`actionInput`、`observation`、`isFinal`、`totalSteps` |
| `complete` | 最终结果：`success`、`answer`、`errorMessage`、`duration`、`totalSteps` |
| `error` | 错误：`error` |

**示例**：

```text
event: step
data: {"stepNumber":1,"thought":"需要查询当前时间","action":"getCurrentTime","actionInput":"{}","observation":"2026-10-04 12:50:00","isFinal":false,"totalSteps":15}

event: complete
data: {"success":true,"answer":"现在是 2026-10-04 12:50:00","duration":1500,"totalSteps":2}
```

#### 4.6.6 推理引擎说明

```http
GET /api/javachain/agent/steps
```

**响应 data**：包含工作流程说明与安全约束（注意：此接口的安全参数为**硬编码描述值**，与 `ReActAgentConfig` 实际配置不一致）。

### 4.7 文档处理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/javachain/document/upload` | 上传文档 |
| POST | `/api/javachain/document/load` | 加载文档 |
| POST | `/api/javachain/document/extract` | 提取文本 |
| GET | `/api/javachain/document/supported-formats` | 查询支持格式 |

**上传**：`multipart/form-data`，文件字段以接口实现为准。

---

## 5. 接口速查表

| 模块 | 接口数 | 网关前缀 |
| --- | --- | --- |
| 认证 | 4 | `/api/system/auth` |
| 用户 | 8 | `/api/system/users` |
| 角色 | 9 | `/api/system/roles` |
| 权限 | 7 | `/api/system/permissions` |
| 菜单 | 7 | `/api/system/menu` |
| 系统看板 | 1 | `/api/system/dashboard` |
| 商品 | 5 | `/api/business/products` |
| 订单 | 5 | `/api/business/orders` |
| Agent 订单 | 3 | `/api/business/agent/orders` |
| 物流 | 8 | `/api/business/express` |
| 营销 | 6 | `/api/business/marketing` |
| 业务看板 | 1 | `/api/business/dashboard` |
| 定时任务 | 10 | `/api/business/api/jobs` |
| AI 会话 | 4 | `/api/javachain/chat/session` |
| AI 对话 | 3 | `/api/javachain/chat` |
| AI 知识库 | 4 | `/api/javachain/chat/rag` |
| AI 文件 | 3 | `/api/javachain/chat/files` |
| AI MCP | 2 + 天气 | `/api/javachain/chat` |
| AI Agent | 7 | `/api/javachain/agent` |
| AI 文档 | 4 | `/api/javachain/document` |

---

## 6. 附录

### 6.1 调用示例

**完整登录并查询商品流程**：

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:8085/api/system/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 携带 Token 查询商品
curl http://localhost:8085/api/business/products?pageNum=1&pageSize=10 \
  -H "Authorization: Bearer <token>"

# 3. 调用 AI 问答
curl -X POST http://localhost:8085/api/javachain/chat/rag \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"question":"公司有哪些产品"}'
```

### 6.2 相关文档

- [产品需求文档](../01-产品/PRD-产品需求文档.md)
- [技术设计文档](../02-技术/TRD-技术设计文档.md)
- [数据库设计文档](../04-数据库/数据库设计文档.md)

### 6.3 待完善接口

以下接口当前为演示或占位实现，对接前请知悉：

| 接口 | 现状 |
| --- | --- |
| `/api/business/api/jobs/**` | 返回硬编码演示数据，未对接 XXL-Job |
| 微信支付相关 | 服务层已实现，配置为占位值，无对外 REST 接口 |

---

## 7. 变更记录

| 版本 | 日期 | 变更内容 | 关联 Issue | 修改人 |
| --- | --- | --- | --- | --- |
| v1.0 | 2026-10-04 | 初版，整理约 90 个接口（system / business / javachain） | — | — |

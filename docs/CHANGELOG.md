# 更新日志（CHANGELOG）

本文件记录**项目级**变更。遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 风格，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

> **与各文档变更记录的区别**
> - 各文档文末的「变更记录」：细粒度，记录"这份文档改了什么"
> - 本文件：粗粒度，记录"这个版本发布了什么"

**何时更新**：每次合并到 `master` 或打 Release 标签时。日常 `dev` 分支开发不更新，合并时统一整理。

**分类标签**：`新增` / `变更` / `弃用` / `移除` / `修复` / `安全` / `文档`

---

## [未发布]

### 修复
- **鉴权越权**：`PermissionCheckUtil` 原先以「用户名 == admin」判定超级管理员，导致名为 `admin` 的普通账号获得全部权限、而真正的管理员被拒绝写操作；改为按「用户 → 角色 → 权限」查库判定，超管以角色标识 `admin` 识别
- **刷新页面后角色与权限丢失**：`UserResponse` 缺 `roles` 字段、`permissions` 恒为空，导致刷新后角色标签消失、菜单不再按角色过滤
- **菜单查询用错字段**：`sys_menu.role_keys` 存角色标识，原代码传入角色名称，查询结果恒为 0 条
- **侧边栏菜单重复**：多角色用户的菜单未按 id 去重
- **`SKILL.md` 解析结果为 0 个步骤**：`SkillParser.STEP_PATTERN` 硬编码 `\n`，无法匹配仓库中 CRLF 换行的技能文件
- **`trigger` 声明被忽略**：改为优先读取 frontmatter 的 `trigger` 字段，未声明时才按描述文本猜测
- **分页越界异常**：10 处内存分页在 `pageNum ≤ 0`、`pageSize ≤ 0` 或超大页码时抛异常，统一由新增的 `PageUtils` 做参数归一化与边界裁剪
- **路由守卫失效**：前端守卫中 `!to.path.startsWith('/')` 恒为 false，页面级访问控制从未生效；修正判定并补充重定向防环与豁免路径
- 网关限流过滤器改用响应式 Redis，避免阻塞事件循环；修正与实际执行顺序相反的注释
- 登录页补充密码显隐切换

### 新增
- javachain 补齐前端已调用但后端缺失的 13 个接口：`/api/skills/**`（Skills 列表 / 重载 / 执行 / 自动执行）、`/api/mcp/**`（Server 列表 / 注销 / 插件登记 / 工具执行）、`/api/plugin-governance/**`（列表 / 启用 / 禁用 / 兼容性判定 / 版本比较）
- 新增 `SkillExecutionService`（Skill 步骤编排执行）与 `PluginGovernanceService`（插件登记表、语义化版本比较、插件包元信息读取）

### 变更
- `McpService` 支持列出与注销 MCP Server；注销会同时挂起该 Server 的自动发现，避免被下一次刷新重新注册回来
- `sys_menu` 补齐缺失的「物流管理」菜单（`/business/logistics`），与 `sql/init.sql` 保持一致，页面重新可从侧边栏进入

### 文档
- 建立 `docs/` 文档体系：PRD、TRD、API 接口文档、数据库设计文档、部署运维手册、开发规范与贡献指南
- 新增[需求开发流程](06-规范/需求开发流程.md)与[变更管理规范](06-规范/变更管理规范.md)，确立 Issue 驱动的变更机制
- 新增[文档编写规范](06-规范/文档编写规范.md)，统一文档结构、标题编号、表达、元素与链接规则
- 新增 `scripts/check-docs.py` 文档规范检查器（标题结构、代码围栏、表格、标记、链接与锚点）
- 新增 `docs/_templates/` 模板目录（变更申请单、需求评审记录、验收记录）
- 新增 `.github/ISSUE_TEMPLATE/` GitHub Issue 模板（变更申请单、缺陷报告）
- 建立 `docs/archive/` 历史版本归档机制
- API 接口文档补充 javachain 的 Skills / MCP 管理 / 插件治理接口（v1.1）
- 精简根 `README.md` 为导航入口

### 安全
- ⚠️ **待处理**：`.env` 与 `.env.cloud` 中包含真实 DeepSeek / DashScope API Key 且已提交入库，需迁移至环境变量并清理提交历史（见下方"已知问题"）

---

## 已知问题（待修复）

> 以下问题在文档编写过程中发现，尚未修复，按优先级排列。

### 安全
- `.env` / `.env.cloud` 含真实 API Key 并已提交（**最高优先级**）
- `system-service` 部分写接口有权限校验（`role:manage` / `permission:manage`），但大量读接口无接口级鉴权

### 一致性
- `jwt.expiration` 配置不一致：gateway 为 10 天，system-service 为 1 天
- `sys_product.category` 存的是分类**名称字符串**而非 `sys_category.id`（反范式）
- 审计字段类型不一致：`sys_menu.create_by` 为 varchar(50)，`marketing.create_by` 为 varchar(100)，其余为 bigint
- 订单状态无枚举定义，取值 1/2/3/4/7/8 需从数据推断

### 性能
- 所有列表接口使用**内存分页**（查全量后 `subList`），数据量增长后有风险。参数越界导致的异常已修复（`PageUtils`），但大数据量下的性能问题仍需改由 SQL `LIMIT` 分页解决

### 实现完整性
- `business-service` 的 `JobController` 返回硬编码演示数据，未对接 XXL-Job Admin
- `javachain` 的 `ChatController.vectorizeFiles` 存在硬编码路径 `D:\agentDemo\javachain\...`
- `AgentController.getStepInfo` 返回的安全信息为硬编码文本，与 `ReActAgentConfig` 不一致
- 微信支付仅有服务层实现，无对外 REST 接口
- 插件治理只登记插件包元信息，未实现插件类的动态加载与工具自动注册
- `.trae/skills/*/SKILL.md` 中声明的工具（如 `weather_query`、`logistics_query`）尚未注册为本地 `@Tool` 方法或远端 MCP 工具，执行时会返回 `Tool not found`

---

## [v1.0.0] - 2026-10-04

### 新增
- OA 智能办公管理系统首个版本
  - 多模块 Spring Cloud 微服务：`gateway` / `system-service` / `business-service` / `javachain`
  - Vue 3 + Element Plus 前端
  - JWT 认证 + RBAC 权限模型
  - LangChain4j 驱动的 AI 能力：RAG 检索、ReAct Agent、MCP 工具调用
- Docker Compose 一键编排

### 文档
- 建立 `CODEBUDDY.md`（AI 助手协作说明）

---

## 变更记录

| 版本 | 日期 | 变更内容 | 关联 Issue | 修改人 |
| --- | --- | --- | --- | --- |
| 未发布 | 2026-10-06 | 修复 9 类缺陷；补齐 javachain 13 个缺失接口 | — | — |
| 未发布 | 2026-10-04 | 建立 CHANGELOG 与文档体系 | — | — |

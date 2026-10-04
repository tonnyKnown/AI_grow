# 更新日志（CHANGELOG）

本文件记录**项目级**变更。遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 风格，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

> **与各文档变更记录的区别**
> - 各文档文末的「变更记录」：细粒度，记录"这份文档改了什么"
> - 本文件：粗粒度，记录"这个版本发布了什么"

**何时更新**：每次合并到 `master` 或打 Release 标签时。日常 `dev` 分支开发不更新，合并时统一整理。

**分类标签**：`新增` / `变更` / `弃用` / `移除` / `修复` / `安全` / `文档`

---

## [未发布]

### 文档
- 建立 `docs/` 文档体系：PRD、TRD、API 接口文档、数据库设计文档、部署运维手册、开发规范与贡献指南
- 新增[需求开发流程](06-规范/需求开发流程.md)与[变更管理规范](06-规范/变更管理规范.md)，确立 Issue 驱动的变更机制
- 新增 `docs/_templates/` 模板目录（变更申请单、需求评审记录、验收记录）
- 新增 `.github/ISSUE_TEMPLATE/` GitHub Issue 模板（变更申请单、缺陷报告）
- 建立 `docs/archive/` 历史版本归档机制
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
- 所有列表接口使用**内存分页**（查全量后 `subList`），数据量增长后有风险

### 实现完整性
- `business-service` 的 `JobController` 返回硬编码演示数据，未对接 XXL-Job Admin
- `javachain` 的 `ChatController.vectorizeFiles` 存在硬编码路径 `D:\agentDemo\javachain\...`
- `AgentController.getStepInfo` 返回的安全信息为硬编码文本，与 `ReActAgentConfig` 不一致
- 微信支付仅有服务层实现，无对外 REST 接口

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
| 未发布 | 2026-10-04 | 建立 CHANGELOG 与文档体系 | — | — |

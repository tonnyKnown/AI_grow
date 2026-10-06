# CODEBUDDY.md

This file provides guidance to CodeBuddy Code when working with code in this repository.

## Project Overview

OA 智能办公管理系统 — a Spring Cloud microservice backend + Vue 3 frontend + JavaChain AI service. The Maven root (`com.oa:oa-system`) aggregates `gateway`, `system-service`, and `business-service`. `javachain` is a **separate, standalone Maven project** (its own `pom.xml`, Spring Boot 3.3.1, no parent) and must be built/run from its own directory. A third-party MCP server (`D:\my_mcp\mysql-plugin`) is an external project referenced only by `docker-compose.yml`.

**Detailed documentation lives in `docs/`** — consult these before re-deriving anything from source:

| Doc | Path |
| --- | --- |
| Documentation index | `docs/README.md` |
| PRD (features, user stories, acceptance criteria) | `docs/01-产品/PRD-产品需求文档.md` |
| TRD (architecture, module design, AI service, ADRs, tech debt) | `docs/02-技术/TRD-技术设计文档.md` |
| API reference (all REST endpoints) | `docs/03-接口/API-接口文档.md` |
| Database design (tables, columns, indexes) | `docs/04-数据库/数据库设计文档.md` |
| Deployment & ops (ports, Docker, troubleshooting) | `docs/05-运维/部署运维手册.md` |
| Dev standards & contribution guide | `docs/06-规范/开发规范与贡献指南.md` |
| Doc authoring standard (structure, headings, formatting) | `docs/06-规范/文档编写规范.md` |
| Requirement/change workflow (Issue-driven 9 steps) | `docs/06-规范/需求开发流程.md` |
| Change/version management rules (versioning, archiving) | `docs/06-规范/变更管理规范.md` |
| Project changelog & known issues | `docs/CHANGELOG.md` |
| Templates (change request, review, acceptance) | `docs/_templates/` |
| GitHub Issue templates | `.github/ISSUE_TEMPLATE/` |
| Historical doc versions (frozen) | `docs/archive/` |

When changing code, keep the matching doc in sync (see `docs/README.md` → 文档维护). When adding a doc, place it in the numbered `docs/<NN-分类>/` directory and register it in `docs/README.md`.

> ⚠️ **Any requirement/behavior change must be Issue-driven** — file a GitHub Issue first, then archive the old doc version to `docs/archive/` **before** editing, bump the doc version, and add a change-record row. Do not edit PRD/TRD/API/DB docs directly. See `docs/06-规范/需求开发流程.md`.

> 📌 **When writing or editing any doc under `docs/`**, follow `docs/06-规范/文档编写规范.md`: single H1, `## N.` numbered H2 (except the trailing unnumbered `## 变更记录`), `### N.M` H3, 4-field blockquote header, `| --- |` table separators, language-tagged code fences (`text` for plain output), markers limited to ✅❌⚠️📌💡, and relative links only (never machine-absolute paths).

**After changing docs, run the checker** (exit 0 = clean):

```bash
python scripts/check-docs.py            # all docs
python scripts/check-docs.py --links-only
```

It validates heading structure, code-fence language tags, table style, markers, link targets, and anchors against `docs/06-规范/文档编写规范.md`. `docs/archive/`, `docs/_templates/`, `README.md`, `CHANGELOG.md` and this file are exempt from the H2-numbering rule.

## Build & Run Commands

All backend services require **JDK 21** and Maven 3.8+. There are no Maven wrapper scripts; use a system `mvn`.

### Root reactor modules (gateway / system-service / business-service)

```bash
mvn clean package                                   # build all root modules
mvn -DskipTests package                             # build without tests (used by Docker flow)
mvn -pl gateway spring-boot:run                     # run a single service (dev)
mvn -pl system-service spring-boot:run
mvn -pl business-service spring-boot:run
mvn -pl gateway clean package && java -jar gateway/target/gateway-1.0.0.jar
```

### javachain (separate project — always `cd javachain` first)

```bash
cd javachain
mvn clean package
mvn spring-boot:run
java -jar target/javachain-1.0.0.jar
```

### Frontend

```bash
cd oa-frontend
npm install
npm run dev        # port 5173, proxies /api -> http://localhost:8085
npm run build      # outputs dist/
npm run preview
```

### Tests

Test coverage is minimal — only `javachain/src/test/java/com/example/javachain/JavachainApplicationTests.java` exists (a context-load test). There is no test source tree in the root reactor modules.

```bash
mvn test                                          # all tests in a module
mvn -pl gateway test
mvn -pl gateway test -Dtest=SomeTestClass         # single test class
mvn -pl gateway test -Dtest=SomeTestClass#someMethod  # single test method
cd javachain && mvn test -Dtest=JavachainApplicationTests
```

### Docker (full local stack)

```bash
mvn -DskipTests package                # 1. build root modules
cd javachain && mvn -DskipTests package && cd ..   # 2. build javachain
cd D:\my_mcp\mysql-plugin && mvn -DskipTests package && cd D:\vue\java-backend  # 3. build MCP plugin
docker compose up -d --build
docker compose logs -f gateway          # tail a service
docker compose down                     # stop
docker compose down -v                  # stop AND wipe MySQL/Redis volumes (re-initializes DB)
```

`.env` overrides base images (defaults point at `docker.m.daocloud.io` mirror) and provides `DEEPSEEK_API_KEY` / `DASHSCOPE_API_KEY`. Copy `.env.docker.example` to `.env` if missing.

### Docker port mapping (host differs from container)

Container ports are remapped on the host to avoid clashing with local installs. **When connecting from the host, use the host port:**

| Service | Host | Container |
| --- | --- | --- |
| MySQL | 3307 | 3306 |
| Redis | 6380 | 6379 |
| Nacos console | 8080 | 8080 |
| XXL-Job Admin | 8088 | 8080 |
| gateway | 8085 | 8085 |
| system-service | 8081 | 8081 |
| business-service | 8082 / 9999 (xxl executor) | same |
| mysql-plugin | 8083 | 8083 |
| javachain | 8087 | 8087 |
| frontend (nginx) | 5173 | 80 |

Nacos console inside Docker: `nacos` / `nOn2h1l8cHSG`. XXL-Job console: `admin` / `123456`.

## Architecture

### Request flow

```text
Browser -> Vite dev 5173 (proxy /api) [or nginx 80 in Docker] -> Gateway 8085 -> microservice
```

The gateway is the single entry point. It uses Nacos service discovery with `lb://<service-name>` URIs and rewrites paths per route:

| Frontend prefix | Target service | Rewrite |
| --- | --- | --- |
| `/api/system/**` | `lb://system-service` | `/api/system/(.*)` → `/system/$1` |
| `/api/business/**` | `lb://business-service` | `StripPrefix=1` (drops `/api`) |
| `/api/javachain/**` | `lb://javachain` | `/api/javachain/(.*)` → `/api/$1` |

**Important:** each service's internal controller prefix differs (`/system`, `/business`, `/api`). When adding endpoints, match the rewrite rule for the owning service or the gateway will 404.

### Authentication (critical cross-service contract)

- `system-service` issues JWTs on `/api/system/auth/login`. `gateway` validates them in `JwtAuthenticationFilter` (a `GlobalFilter` at order `-2000000`), then injects `userId` and `X-Username` headers downstream.
- The JWT `secret` **must be identical** across `gateway`, `system-service`, `business-service`, and `javachain`. All four currently default to `oa-system-jwt-secret-key-2024-for-secure-authentication-very-long-enough` (via `jwt.secret` in each `application.yml`). Changing it in one place without the others silently breaks all auth.
- Gateway whitelist (no token required): `/api/system/auth/login`, `/api/system/auth/logout`, `/api/python/**`. Service-to-service calls may bypass JWT by sending a `Service-Key` header equal to `gateway.trusted-services.python-service-key`.
- Frontend stores the token in `localStorage`, adds `Authorization: Bearer <token>` via an Axios request interceptor, and force-redirects to `/login` on 401 (see `oa-frontend/src/utils/request.js`). The response interceptor expects the `{ code, message, data }` envelope and treats `code !== 200` as an error. 502/503/timeout redirect to `/maintenance`.

### javachain AI service (the most complex module)

Located at `javachain/src/main/java/com/example/javachain`. Its service name (`spring.application.name: javachain`) is hard-coded in both `bootstrap.yml` and `application.yml` and **must match the gateway's `lb://javachain` route**.

- **LLM provider** is switchable via `javachain.llm.provider` (`deepseek` default, or `ollama`). `DeepSeekConfig` / `OllamaConfig` / `DashScopeConfig` produce the `ChatLanguageModel` bean. Embeddings for RAG come from DashScope (`text-embedding-v1`), configured separately from the chat provider.
- **Config import chain:** `application.yml` and `bootstrap.yml` both `spring.config.import` optional `.env` / `application.yml` files from `./`, `./javachain/`, and `../`. This lets secrets live in a root `.env` (gitignored) while keeping YAML defaults in the repo.
- **Vector store is in-memory** (`vector-store.type: memory`). Knowledge-base contents are lost on restart. To persist, swap the `EmbeddingStore` bean in `VectorStoreConfig`.
- **RAG pipeline** (`RagService`): query expansion (keyword + synonym rewrite) → parallel hybrid retrieval (vector search + BM25 via `BM25Retriever`, both on a fixed thread pool) → LLM rerank when >3 candidates (falls back to rule-based scoring) → answer generation with `[文档X]` citations. HanLP (`KeywordExtractionService`) does Chinese keyword extraction.
- **ReAct Agent** (`ReActAgent`) is prompt-driven, not a library agent: it builds a system prompt listing the tools from `McpService.getAvailableTools()`, calls the `ChatLanguageModel`, and regex-parses `思考:` / `Action:` / `ActionInput:` / `Final Answer:` from the response. All limits live in `ReActAgentConfig` (`javachain.agent.*`): `maxSteps` 15, `maxToolCallsPerTool` 3, `timeoutMs` 60000, `maxConsecutiveFailures` 2, retry count/delay, tool-list cache TTL, `sensitiveKeywords`, and `dangerousTools`. Dangerous tools pause the run and return a `pendingConfirmation` result; `AgentController` exposes start/status/confirm/stream endpoints for this human-in-the-loop flow. Long jobs run on an `ExecutorService` and are tracked in `JobContext` (in-memory).
- **Tool exposure** has two layers in `McpService`:
  1. **Local tools** — methods annotated with LangChain4j `@Tool` on `SkillService` (e.g. `getCurrentTime`, `generateUUID`, `queryKnowledgeBase`, `getCurrentUserFromToken`). `McpService` discovers them by reflection, builds JSON schemas from parameter types, and invokes them directly. `getCurrentUserFromToken` reads the `Authorization` header from the current request.
  2. **Remote MCP tools** — discovered via Nacos MCP Registry (`McpServiceDiscoverer` hits `/nacos/v3/admin/ai/mcp/list`) and called over the MCP SSE transport (JSON-RPC 2.0 `initialize` → `tools/list` → `tools/call`). If Nacos discovery finds nothing, it falls back to `mcp.fallback-base-url` (default `http://127.0.0.1:8083`, the mysql-plugin). Tool name matching normalizes underscores/hyphens and tries snake_case↔camelCase.
- **Skills** (`SkillRegistry`) are markdown files matching `skill.pattern` (default `.trae/skills/*/SKILL.md`), parsed by `SkillParser` into trigger words + steps, indexed by name and trigger. Add a new skill by dropping a directory with a `SKILL.md` under `javachain/src/main/resources/.trae/skills/` (bundled at build time via the `<includes>` rule in `javachain/pom.xml`). Skills can be reloaded at runtime through the API.
- `mcp.auto-load: false` — MCP plugins are not auto-loaded at startup; they are loaded on demand.

### business-service

Standard Spring Boot MVC + MyBatis. Notables beyond CRUD:

- **XXL-Job executor** embedded (`XxlJobConfig`, `DemoJobHandler`, `TodoTaskJob`, `JobRegistryService`); `JobController` proxies job list/start/stop/trigger/log to the XXL-Job Admin REST API. Executor port 9999.
- **Express tracking** (`ExpressQueryService` / `SfExpressQueryServiceImpl`) and **WeChat Pay** (`WechatPayService` / `WechatPayUtil` / `WechatPayConfig`). Both have placeholder credentials in `application.yml` (`wechat.pay.*`) that must be filled for real use.
- **Agent-facing order endpoints** (`AgentOrderController` under `/business/agent/orders/**`) are designed to be called by the AI service.
- Calls `system-service` via OpenFeign (`SystemServiceFeign`, default URL `http://localhost:8081`, overridable with `feign.system-service.url`).
- `ApiLog` / `ApiLogAspect` provide AOP-based API logging; `Result` is the common response envelope; `JwtUtil` parses the gateway-forwarded identity.

### system-service

RBAC core: `AuthController` (+ `DashboardController`, `UserController`, `RoleController`, `PermissionController`, `MenuController`) with `entity`/`mapper`/`service` layering for users, roles, permissions, menus, and their join tables. Uses `BCryptPasswordEncoder`. `SecurityConfig` disables CSRF and **permits all requests** — authorization is enforced at the gateway plus `JwtBlacklistInterceptor` / `PermissionCheckUtil`, not by Spring Security method rules. MyBatis XML mappers live in `src/main/resources/mapper/`.

### oa-frontend

Vue 3 + Vite + Pinia + Element Plus. `src/api/*.js` holds one module per backend domain; `src/stores/user.js` holds auth/menu state; `src/directives/permission.js` implements a permission directive; `src/router/index.js` defines routes under a single `Layout.vue` shell with a global auth guard that checks `localStorage.token`. `@` aliases to `src`. Note the router also has `/business/logistics` (`LogisticsManagement.vue`) which is not listed in the top-level README's route table.

## Database

`sql/init.sql` is the **single source of truth** for the business schema and is auto-run by the MySQL container on first boot. It contains `DROP TABLE` statements and seed data. Tables: `sys_user`, `sys_role`, `sys_permission`, `sys_menu`, `sys_user_role`, `sys_role_permission`, `sys_category`, `sys_product`, `sys_order`, `marketing`. `sql/migration_express.sql` adds express/logistics tables to an existing database. `sql/xxl_job.sql` initializes the XXL-Job Admin schema in a separate `xxl_job` database.

Init scripts in `/docker-entrypoint-initdb.d/` only run on an **empty** data volume — to re-initialize, use `docker compose down -v` first (this deletes data).

## Conventions & Gotchas

- **Secrets are hard-coded in committed files.** `application.yml` files, `docker-compose.yml`, and `.env` (currently committed despite `.gitignore` listing it) contain real-looking DeepSeek/DashScope keys, Nacos passwords, and a MySQL password placeholder (`your_actual_password`). Do not add new real credentials; prefer environment variables. If asked to rotate keys, remember they appear in multiple places.
- Sibling files exist in more than one module (e.g. `Result.java`, `JwtUtil.java`, `DashboardStats.java`, `PageResponse.java`) — they are intentional per-service copies with different packages, not shared code. There is no common library module; do not refactor them into one without being asked.
- `.trae/` directories hold skill markdown and are gitignored at the root but **deliberately included** as build resources for javachain.
- `logs/` and `javachain/temp/`, `javachain/tempFile/` are runtime output and gitignored.
- JDK 21 is mandatory everywhere; a mismatched local JDK is the most common build failure.

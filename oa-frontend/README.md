# OA Frontend 前端项目

`oa-frontend` 是 OA 智能办公管理系统的前端应用，基于 Vue 3 和 Vite 构建，负责登录、权限菜单、系统管理、业务管理、智能聊天、知识库问答和 JavaChain Agent 页面。

## 技术栈

- Vue 3.5
- Vite 5.4
- Vue Router 4.4
- Pinia 2.2
- Element Plus 2.8
- Axios
- Markdown-it

## 目录结构

```text
oa-frontend
├── src
│   ├── api             # 后端接口封装
│   ├── directives      # 权限指令
│   ├── router          # 前端路由
│   ├── stores          # Pinia 状态管理
│   ├── utils           # Axios 请求封装
│   ├── views           # 页面组件
│   ├── App.vue
│   └── main.js
├── vite.config.js      # Vite 配置和代理
├── package.json
└── index.html
```

## 页面路由

| 路由 | 页面 |
| --- | --- |
| `/login` | 登录页 |
| `/dashboard` | 数据看板 |
| `/system/users` | 用户管理 |
| `/system/roles` | 角色管理 |
| `/system/permissions` | 权限管理 |
| `/system/menus` | 菜单管理 |
| `/business/products` | 商品管理 |
| `/business/orders` | 订单管理 |
| `/business/marketing` | 营销管理 |
| `/business/chat` | 智能聊天 |
| `/business/knowledge` | 知识库问答 |
| `/business/javachain` | JavaChain Agent |

## 接口访问

Axios 基础路径配置为：

```text
/api
```

开发环境下，Vite 将 `/api` 代理到网关：

```text
http://localhost:8085
```

因此前端接口调用关系为：

```text
浏览器 -> Vite Dev Server 5173 -> Gateway 8085 -> 后端微服务
```

## 启动方式

安装依赖：

```powershell
npm install
```

启动开发环境：

```powershell
npm run dev
```

浏览器访问：

```text
http://localhost:5173
```

## 构建方式

```powershell
npm run build
```

构建产物输出到：

```text
dist/
```

## 请求与鉴权

- 登录成功后 Token 存入 `localStorage`。
- Axios 请求拦截器会自动添加 `Authorization: Bearer <token>`。
- 响应码为 `401` 时会清理本地 Token 并跳转登录页。
- 路由守卫会拦截未登录用户访问业务页面。

## 注意事项

- 后端网关必须先启动，否则前端接口请求会失败。
- 生产部署时需要把 `/api` 转发到 `gateway` 服务。
- 不要提交 `node_modules/` 和 `dist/`。

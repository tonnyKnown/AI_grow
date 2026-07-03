# OA系统 - A股市场模块

## 项目简介

这是一个基于Vue 3 + Element Plus的OA系统，包含了完整的A股市场实时查看模块。

## 项目结构

```
oa-frontend/
├── src/
│   ├── api/           # API接口
│   ├── views/         # 页面组件
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia状态管理
│   └── utils/         # 工具函数
├── backend/           # 后端服务
│   ├── routes/        # 后端路由
│   └── server.js      # 后端入口
└── ...
```

## 快速开始

### 方式一：分别启动前后端（推荐）

#### 1. 启动后端服务

```bash
cd backend
npm install
npm start
```

后端服务将在 `http://localhost:3000` 启动

#### 2. 启动前端服务

新开一个终端：

```bash
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### 方式二：使用concurrently同时启动（需要先安装concurrently）

```bash
npm install -g concurrently
npm run all
```

## 功能模块

### 1. A股市场模块

**路径**: `/stock`

**功能**:
- 大盘指数展示（上证指数、深证成指、创业板指）
- 股票列表（15只热门A股）
- 股票搜索和行业筛选
- 股票详情查看
- K线图展示

**包含股票**:
- 贵州茅台 (600519)
- 五粮液 (000858)
- 比亚迪 (002594)
- 宁德时代 (300750)
- 浦发银行 (600000)
- ...等15只热门股票

### 2. 其他模块

- 用户管理
- 角色管理
- 权限管理
- 商品管理
- 订单管理
- 菜单管理

## 后端API

### 股票相关接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取股票列表 | GET | /api/stock/list | 获取所有股票，支持搜索和筛选 |
| 获取股票详情 | GET | /api/stock/detail/:code | 获取单只股票的详细信息和K线 |
| 获取行业分类 | GET | /api/stock/categories | 获取所有行业分类 |
| 获取大盘指数 | GET | /api/stock/index | 获取三大指数数据 |

详细API文档请参考 `backend/README.md`

## 技术栈

### 前端
- Vue 3
- Element Plus
- Pinia
- Vue Router
- Axios
- Vite

### 后端
- Node.js
- Express
- CORS

## 构建生产版本

```bash
npm run build
```

## 开发说明

1. 前端请求通过Vite代理转发到后端
2. 所有API请求基础路径为 `/api`
3. 后端默认监听3000端口
4. 前端默认运行在5173端口

## 注意事项

- 请确保先启动后端服务再使用前端
- 如果端口被占用，请修改相应配置
- 后端使用内存数据，重启后数据会重置

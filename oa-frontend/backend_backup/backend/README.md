# OA系统后端服务

## 安装依赖

```bash
cd backend
npm install
```

## 启动服务

```bash
npm start
```

或者开发模式：

```bash
npm run dev
```

服务将在 http://localhost:3000 启动

## API接口文档

### 股票相关接口

#### 1. 获取股票列表
- **接口**: `GET /api/stock/list`
- **参数**:
  - `keyword`: 搜索关键词（可选）
  - `category`: 行业分类（可选，默认all）
  - `sortField`: 排序字段（可选，默认changePercent）
  - `sortOrder`: 排序方式（可选，desc/asc）
- **响应**:
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 15
  }
}
```

#### 2. 获取股票详情
- **接口**: `GET /api/stock/detail/:code`
- **参数**:
  - `code`: 股票代码（路径参数）
- **响应**:
```json
{
  "code": 200,
  "data": {
    "code": "600519",
    "name": "贵州茅台",
    ...
    "kline": [...]
  }
}
```

#### 3. 获取行业分类
- **接口**: `GET /api/stock/categories`
- **响应**:
```json
{
  "code": 200,
  "data": ["银行", "白酒", "保险", ...]
}
```

#### 4. 获取大盘指数
- **接口**: `GET /api/stock/index`
- **响应**:
```json
{
  "code": 200,
  "data": [
    {
      "name": "上证指数",
      "code": "000001",
      "price": 3150.25,
      "change": 15.30,
      "changePercent": 0.49
    },
    ...
  ]
}
```

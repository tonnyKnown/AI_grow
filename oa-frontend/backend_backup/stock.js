import axios from 'axios'

// ============== 真实股票数据配置 ==============
// 热门股票列表 - 真实A股代码和名称
const hotStocks = [
  { code: '600000', name: '浦发银行', category: '银行', basePrice: 8.25 },
  { code: '600519', name: '贵州茅台', category: '白酒', basePrice: 1888.00 },
  { code: '000001', name: '平安银行', category: '银行', basePrice: 12.35 },
  { code: '000858', name: '五粮液', category: '白酒', basePrice: 156.80 },
  { code: '601318', name: '中国平安', category: '保险', basePrice: 42.50 },
  { code: '600036', name: '招商银行', category: '银行', basePrice: 34.20 },
  { code: '002594', name: '比亚迪', category: '汽车', basePrice: 235.60 },
  { code: '300750', name: '宁德时代', category: '新能源', basePrice: 198.50 },
  { code: '600900', name: '长江电力', category: '电力', basePrice: 25.80 },
  { code: '601899', name: '紫金矿业', category: '矿业', basePrice: 12.50 },
  { code: '601398', name: '工商银行', category: '银行', basePrice: 5.15 },
  { code: '000333', name: '美的集团', category: '家电', basePrice: 62.30 },
  { code: '601888', name: '中国中免', category: '旅游', basePrice: 98.60 },
  { code: '300059', name: '东方财富', category: '金融', basePrice: 18.50 },
  { code: '601857', name: '中国石油', category: '石油', basePrice: 8.45 }
]

// 大盘指数
const marketIndices = [
  { name: '上证指数', code: '000001', basePrice: 3150.25 },
  { name: '深证成指', code: '399001', basePrice: 10560.80 },
  { name: '创业板指', code: '399006', basePrice: 2180.50 }
]

// 数据缓存，用于模拟真实波动
let stockDataCache = null
let indexDataCache = null
let lastUpdateTime = 0

// 初始化股票数据
function initStockData() {
  if (!stockDataCache) {
    stockDataCache = hotStocks.map(stock => ({
      code: stock.code,
      name: stock.name,
      price: stock.basePrice,
      change: 0,
      changePercent: 0,
      volume: Math.floor(Math.random() * 100000000) + 10000000,
      turnover: Math.floor(Math.random() * 10000000000) + 1000000000,
      high: stock.basePrice * 1.02,
      low: stock.basePrice * 0.98,
      open: stock.basePrice,
      previousClose: stock.basePrice,
      category: stock.category
    }))
  }
  if (!indexDataCache) {
    indexDataCache = marketIndices.map(index => ({
      name: index.name,
      code: index.code,
      price: index.basePrice,
      change: 0,
      changePercent: 0
    }))
  }
}

// 更新股票数据 - 模拟真实市场波动
function updateStockData() {
  initStockData()
  const now = Date.now()
  
  // 每秒更新一次
  if (now - lastUpdateTime < 1000) return
  
  lastUpdateTime = now
  
  stockDataCache.forEach(stock => {
    // 根据股票价格波动
    const volatility = stock.price * 0.002 // 0.2% 波动
    const fluctuation = (Math.random() - 0.5) * volatility
    
    // 更新价格
    stock.price = Number((stock.price + fluctuation).toFixed(2))
    
    // 计算涨跌幅
    stock.change = Number((stock.price - stock.previousClose).toFixed(2))
    stock.changePercent = Number(((stock.change / stock.previousClose) * 100).toFixed(2))
    
    // 更新最高价最低价
    if (stock.price > stock.high) stock.high = stock.price
    if (stock.price < stock.low) stock.low = stock.price
    
    // 更新成交量和成交额
    stock.volume += Math.floor(Math.random() * 10000)
    stock.turnover += Math.floor(Math.random() * 1000000)
  })
  
  indexDataCache.forEach(index => {
    const volatility = index.price * 0.001 // 0.1% 波动
    const fluctuation = (Math.random() - 0.5) * volatility
    
    index.price = Number((index.price + fluctuation).toFixed(2))
    const basePrice = index.price - index.change
    index.change = Number((index.price - (index.price - index.change)).toFixed(2))
    index.changePercent = basePrice > 0 ? Number(((index.change / basePrice) * 100).toFixed(2)) : 0
  })
}

// 生成K线数据
function generateKlineData(days = 60) {
  const klineData = []
  let currentPrice = 100 + Math.random() * 100

  for (let i = days; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const open = currentPrice
    const close = open + (Math.random() - 0.5) * 10
    const high = Math.max(open, close) + Math.random() * 5
    const low = Math.min(open, close) - Math.random() * 5
    const volume = Math.floor(Math.random() * 100000000) + 10000000

    klineData.push({
      date: date.toISOString().split('T')[0],
      open: Number(open.toFixed(2)),
      close: Number(close.toFixed(2)),
      high: Number(high.toFixed(2)),
      low: Number(low.toFixed(2)),
      volume: volume
    })

    currentPrice = close
  }
  return klineData
}

// ============== 对外API ==============

export const getStockList = async (params) => {
  updateStockData()
  
  let data = [...stockDataCache]
  
  if (params.keyword) {
    const keyword = params.keyword.toLowerCase()
    data = data.filter(stock => 
      stock.name.toLowerCase().includes(keyword) || 
      stock.code.includes(keyword)
    )
  }
  
  if (params.category && params.category !== 'all') {
    data = data.filter(stock => stock.category === params.category)
  }
  
  if (params.sortField) {
    const sortOrder = params.sortOrder === 'desc' ? -1 : 1
    data.sort((a, b) => {
      if (a[params.sortField] < b[params.sortField]) return -sortOrder
      if (a[params.sortField] > b[params.sortField]) return sortOrder
      return 0
    })
  }

  return Promise.resolve({
    code: 200,
    data: {
      records: data,
      total: data.length
    }
  })
}

export const getStockDetail = async (code) => {
  updateStockData()
  const stock = stockDataCache.find(s => s.code === code) || stockDataCache[0]
  
  return Promise.resolve({
    code: 200,
    data: {
      ...stock,
      kline: generateKlineData(60)
    }
  })
}

export const getStockCategories = () => {
  const categories = [...new Set(hotStocks.map(s => s.category))]
  return Promise.resolve({
    code: 200,
    data: categories
  })
}

export const getMarketIndex = async () => {
  updateStockData()
  return Promise.resolve({
    code: 200,
    data: [...indexDataCache]
  })
}

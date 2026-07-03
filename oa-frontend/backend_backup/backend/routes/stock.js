const express = require('express')
const router = express.Router()

const mockStockData = [
  { code: '600000', name: '浦发银行', price: 8.25, change: 0.12, changePercent: 1.47, volume: 123456789, turnover: 1023456789, high: 8.30, low: 8.10, open: 8.15, previousClose: 8.13, category: '银行' },
  { code: '600519', name: '贵州茅台', price: 1888.00, change: 22.50, changePercent: 1.21, volume: 123456, turnover: 23456789000, high: 1900.00, low: 1860.00, open: 1865.50, previousClose: 1865.50, category: '白酒' },
  { code: '000001', name: '平安银行', price: 12.35, change: -0.15, changePercent: -1.20, volume: 234567890, turnover: 2900000000, high: 12.55, low: 12.20, open: 12.50, previousClose: 12.50, category: '银行' },
  { code: '000858', name: '五粮液', price: 156.80, change: 3.20, changePercent: 2.08, volume: 8765432, turnover: 1376543210, high: 158.00, low: 152.50, open: 153.60, previousClose: 153.60, category: '白酒' },
  { code: '601318', name: '中国平安', price: 42.50, change: 0.80, changePercent: 1.92, volume: 45678901, turnover: 1948765432, high: 43.00, low: 41.50, open: 41.70, previousClose: 41.70, category: '保险' },
  { code: '600036', name: '招商银行', price: 34.20, change: -0.45, changePercent: -1.30, volume: 98765432, turnover: 3387654321, high: 35.00, low: 34.00, open: 34.65, previousClose: 34.65, category: '银行' },
  { code: '002594', name: '比亚迪', price: 235.60, change: 5.20, changePercent: 2.25, volume: 23456789, turnover: 5525432100, high: 238.00, low: 229.50, open: 230.40, previousClose: 230.40, category: '汽车' },
  { code: '300750', name: '宁德时代', price: 198.50, change: -2.30, changePercent: -1.15, volume: 15678901, turnover: 3105432109, high: 202.00, low: 196.00, open: 200.80, previousClose: 200.80, category: '新能源' },
  { code: '600900', name: '长江电力', price: 25.80, change: 0.30, changePercent: 1.18, volume: 34567890, turnover: 893456789, high: 26.00, low: 25.30, open: 25.50, previousClose: 25.50, category: '电力' },
  { code: '601899', name: '紫金矿业', price: 12.50, change: 0.20, changePercent: 1.63, volume: 123456789, turnover: 1545678901, high: 12.60, low: 12.20, open: 12.30, previousClose: 12.30, category: '矿业' },
  { code: '601398', name: '工商银行', price: 5.15, change: 0.05, changePercent: 0.98, volume: 234567890, turnover: 1209678901, high: 5.18, low: 5.08, open: 5.10, previousClose: 5.10, category: '银行' },
  { code: '000333', name: '美的集团', price: 62.30, change: 1.10, changePercent: 1.80, volume: 12345678, turnover: 770456789, high: 63.00, low: 60.80, open: 61.20, previousClose: 61.20, category: '家电' },
  { code: '601888', name: '中国中免', price: 98.60, change: -1.20, changePercent: -1.20, volume: 8765432, turnover: 864678901, high: 100.50, low: 97.80, open: 99.80, previousClose: 99.80, category: '旅游' },
  { code: '300059', name: '东方财富', price: 18.50, change: 0.35, changePercent: 1.93, volume: 234567890, turnover: 4348765432, high: 18.80, low: 17.90, open: 18.15, previousClose: 18.15, category: '金融' },
  { code: '601857', name: '中国石油', price: 8.45, change: -0.10, changePercent: -1.17, volume: 123456789, turnover: 1043456789, high: 8.60, low: 8.35, open: 8.55, previousClose: 8.55, category: '石油' }
]

function generateKlineData(days = 30) {
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

router.get('/list', (req, res) => {
  const { keyword = '', category = 'all', sortField = 'changePercent', sortOrder = 'desc' } = req.query
  let data = [...mockStockData]
  
  if (keyword) {
    const keywordLower = keyword.toLowerCase()
    data = data.filter(stock => 
      stock.name.toLowerCase().includes(keywordLower) || 
      stock.code.includes(keyword)
    )
  }
  
  if (category && category !== 'all') {
    data = data.filter(stock => stock.category === category)
  }
  
  if (sortField) {
    const sortOrderNum = sortOrder === 'desc' ? -1 : 1
    data.sort((a, b) => {
      if (a[sortField] < b[sortField]) return -sortOrderNum
      if (a[sortField] > b[sortField]) return sortOrderNum
      return 0
    })
  }

  res.json({
    code: 200,
    data: {
      records: data,
      total: data.length
    }
  })
})

router.get('/detail/:code', (req, res) => {
  const code = req.params.code
  const stock = mockStockData.find(s => s.code === code)
  
  if (!stock) {
    return res.json({
      code: 404,
      message: '股票不存在'
    })
  }

  res.json({
    code: 200,
    data: {
      ...stock,
      kline: generateKlineData(60)
    }
  })
})

router.get('/categories', (req, res) => {
  const categories = [...new Set(mockStockData.map(s => s.category))]
  res.json({
    code: 200,
    data: categories
  })
})

router.get('/index', (req, res) => {
  res.json({
    code: 200,
    data: [
      { name: '上证指数', code: '000001', price: 3150.25, change: 15.30, changePercent: 0.49 },
      { name: '深证成指', code: '399001', price: 10560.80, change: 68.50, changePercent: 0.65 },
      { name: '创业板指', code: '399006', price: 2180.50, change: 10.20, changePercent: 0.47 }
    ]
  })
})

module.exports = router

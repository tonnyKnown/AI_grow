const express = require('express')
const cors = require('cors')
const stockRoutes = require('./routes/stock')

const app = express()
const PORT = 3000

app.use(cors())
app.use(express.json())

app.get('/', (req, res) => {
  res.json({ message: 'OA系统后端服务已启动', version: '1.0.0' })
})

app.use('/api/stock', stockRoutes)

app.listen(PORT, () => {
  console.log(`后端服务已启动: http://localhost:${PORT}`)
})

const express = require('express')
const cors = require('cors')
const authRoutes = require('./routes/auth')
const marketingRoutes = require('./routes/marketing')
const userRoutes = require('./routes/user')
const roleRoutes = require('./routes/role')
const permissionRoutes = require('./routes/permission')
const productRoutes = require('./routes/product')
const orderRoutes = require('./routes/order')
const menuRoutes = require('./routes/menu')

const app = express()
const PORT = 8081

app.use(cors())
app.use(express.json())

app.use((req, res, next) => {
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  next();
});

app.get('/', (req, res) => {
  res.json({ message: 'OA系统后端服务已启动', version: '1.0.0' })
})

app.use('/api/auth', authRoutes)
app.use('/api/marketing', marketingRoutes)
app.use('/api/users', userRoutes)
app.use('/api/roles', roleRoutes)
app.use('/api/permissions', permissionRoutes)
app.use('/api/products', productRoutes)
app.use('/api/orders', orderRoutes)
app.use('/api/menus', menuRoutes)
app.use('/api/menu', menuRoutes)

app.listen(PORT, () => {
  console.log(`后端服务已启动: http://localhost:${PORT}`)
})
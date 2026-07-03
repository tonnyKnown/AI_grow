const express = require('express');
const { products } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  const { name, category, pageNum = 1, pageSize = 10 } = req.query;

  let data = [...products];

  if (name) {
    data = data.filter(item => item.name.includes(name));
  }

  if (category) {
    data = data.filter(item => item.category === category);
  }

  const total = data.length;
  const start = (pageNum - 1) * pageSize;
  const end = start + parseInt(pageSize);
  const list = data.slice(start, end);

  res.json({
    code: 200,
    data: {
      list,
      total,
      pageNum: parseInt(pageNum),
      pageSize: parseInt(pageSize)
    }
  });
});

router.get('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const product = products.find(p => p.id === parseInt(id));

  if (!product) {
    return res.status(404).json({ code: 404, message: '商品不存在' });
  }

  res.json({
    code: 200,
    data: product
  });
});

router.post('/', verifyToken, (req, res) => {
  const { name, category, price, stock, status = 1 } = req.body;

  if (!name || !price) {
    return res.status(400).json({ code: 400, message: '商品名称和价格不能为空' });
  }

  const now = new Date().toLocaleString();
  const newProduct = {
    id: Date.now(),
    name,
    category: category || '其他',
    price: parseFloat(price),
    stock: parseInt(stock) || 0,
    status: parseInt(status),
    createdAt: now,
    updatedAt: now
  };

  products.push(newProduct);

  res.json({
    code: 200,
    message: '创建成功',
    data: newProduct
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { name, category, price, stock, status } = req.body;

  const index = products.findIndex(p => p.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '商品不存在' });
  }

  products[index] = {
    ...products[index],
    name: name || products[index].name,
    category: category || products[index].category,
    price: price !== undefined ? parseFloat(price) : products[index].price,
    stock: stock !== undefined ? parseInt(stock) : products[index].stock,
    status: status !== undefined ? parseInt(status) : products[index].status,
    updatedAt: new Date().toLocaleString()
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: products[index]
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = products.findIndex(p => p.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '商品不存在' });
  }

  products.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

module.exports = router;
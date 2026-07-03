const express = require('express');
const { orders } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  const { orderNo, status, pageNum = 1, pageSize = 10 } = req.query;

  let data = [...orders];

  if (orderNo) {
    data = data.filter(item => item.orderNo.includes(orderNo));
  }

  if (status) {
    data = data.filter(item => item.status === status);
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
  const order = orders.find(o => o.id === parseInt(id));

  if (!order) {
    return res.status(404).json({ code: 404, message: '订单不存在' });
  }

  res.json({
    code: 200,
    data: order
  });
});

router.post('/', verifyToken, (req, res) => {
  const { productName, quantity, totalPrice, status = 'pending', userId } = req.body;

  if (!productName || !quantity || !totalPrice) {
    return res.status(400).json({ code: 400, message: '请填写完整订单信息' });
  }

  const now = new Date();
  const orderNo = `ORD${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(orders.length + 1).padStart(3, '0')}`;

  const newOrder = {
    id: Date.now(),
    orderNo,
    productName,
    quantity: parseInt(quantity),
    totalPrice: parseFloat(totalPrice),
    status,
    userId: parseInt(userId) || 1,
    createdAt: now.toLocaleString()
  };

  orders.push(newOrder);

  res.json({
    code: 200,
    message: '创建成功',
    data: newOrder
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { status } = req.body;

  const index = orders.findIndex(o => o.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '订单不存在' });
  }

  orders[index] = {
    ...orders[index],
    status: status || orders[index].status
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: orders[index]
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = orders.findIndex(o => o.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '订单不存在' });
  }

  orders.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

module.exports = router;
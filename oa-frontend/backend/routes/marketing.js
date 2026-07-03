const express = require('express');
const { marketingActivities } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  const { name, status, pageNum = 1, pageSize = 10 } = req.query;

  let data = [...marketingActivities];

  if (name) {
    data = data.filter(item => item.name.includes(name));
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
  const activity = marketingActivities.find(item => item.id === parseInt(id));

  if (!activity) {
    return res.status(404).json({ code: 404, message: '活动不存在' });
  }

  res.json({
    code: 200,
    data: activity
  });
});

router.post('/', verifyToken, (req, res) => {
  const { name, type, discount, startTime, endTime, description } = req.body;

  if (!name || !type || !discount || !startTime || !endTime) {
    return res.status(400).json({ code: 400, message: '请填写完整信息' });
  }

  const now = new Date();
  let status = 'pending';
  if (startTime <= now.toISOString().slice(0, 19).replace('T', ' ') && 
      endTime >= now.toISOString().slice(0, 19).replace('T', ' ')) {
    status = 'active';
  } else if (endTime < now.toISOString().slice(0, 19).replace('T', ' ')) {
    status = 'ended';
  }

  const newActivity = {
    id: Date.now(),
    name,
    type,
    discount: parseFloat(discount),
    startTime,
    endTime,
    status,
    description: description || '',
    createdAt: now.toLocaleString(),
    updatedAt: now.toLocaleString()
  };

  marketingActivities.push(newActivity);

  res.json({
    code: 200,
    message: '创建成功',
    data: newActivity
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { name, type, discount, startTime, endTime, description } = req.body;

  const index = marketingActivities.findIndex(item => item.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '活动不存在' });
  }

  const now = new Date();
  let status = 'pending';
  if (startTime <= now.toISOString().slice(0, 19).replace('T', ' ') && 
      endTime >= now.toISOString().slice(0, 19).replace('T', ' ')) {
    status = 'active';
  } else if (endTime < now.toISOString().slice(0, 19).replace('T', ' ')) {
    status = 'ended';
  }

  marketingActivities[index] = {
    ...marketingActivities[index],
    name,
    type,
    discount: parseFloat(discount),
    startTime,
    endTime,
    status,
    description: description || '',
    updatedAt: now.toLocaleString()
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: marketingActivities[index]
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = marketingActivities.findIndex(item => item.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '活动不存在' });
  }

  marketingActivities.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

module.exports = router;
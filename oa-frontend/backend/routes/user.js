const express = require('express');
const bcrypt = require('bcryptjs');
const { users } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  const { username, pageNum = 1, pageSize = 10 } = req.query;

  let data = users.map(u => ({
    id: u.id,
    username: u.username,
    realName: u.realName,
    email: u.email,
    phone: u.phone,
    status: u.status,
    roles: u.roles,
    createdAt: u.createdAt,
    updatedAt: u.updatedAt
  }));

  if (username) {
    data = data.filter(item => item.username.includes(username));
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
  const user = users.find(u => u.id === parseInt(id));

  if (!user) {
    return res.status(404).json({ code: 404, message: '用户不存在' });
  }

  res.json({
    code: 200,
    data: {
      id: user.id,
      username: user.username,
      realName: user.realName,
      email: user.email,
      phone: user.phone,
      status: user.status,
      roles: user.roles,
      createdAt: user.createdAt,
      updatedAt: user.updatedAt
    }
  });
});

router.post('/', verifyToken, (req, res) => {
  const { username, password, realName, email, phone, status = 1, roles = [] } = req.body;

  if (!username || !password) {
    return res.status(400).json({ code: 400, message: '用户名和密码不能为空' });
  }

  const exists = users.find(u => u.username === username);
  if (exists) {
    return res.status(400).json({ code: 400, message: '用户名已存在' });
  }

  const now = new Date().toLocaleString();
  const newUser = {
    id: Date.now(),
    username,
    password: bcrypt.hashSync(password, 10),
    realName: realName || username,
    email: email || '',
    phone: phone || '',
    status: parseInt(status),
    roles: Array.isArray(roles) ? roles : [],
    permissions: [],
    createdAt: now,
    updatedAt: now
  };

  users.push(newUser);

  res.json({
    code: 200,
    message: '创建成功',
    data: {
      id: newUser.id,
      username: newUser.username,
      realName: newUser.realName,
      email: newUser.email,
      phone: newUser.phone,
      status: newUser.status,
      roles: newUser.roles,
      createdAt: newUser.createdAt,
      updatedAt: newUser.updatedAt
    }
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { realName, email, phone, status, roles } = req.body;

  const index = users.findIndex(u => u.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '用户不存在' });
  }

  users[index] = {
    ...users[index],
    realName: realName || users[index].realName,
    email: email || users[index].email,
    phone: phone || users[index].phone,
    status: status !== undefined ? parseInt(status) : users[index].status,
    roles: roles !== undefined ? (Array.isArray(roles) ? roles : users[index].roles) : users[index].roles,
    updatedAt: new Date().toLocaleString()
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: {
      id: users[index].id,
      username: users[index].username,
      realName: users[index].realName,
      email: users[index].email,
      phone: users[index].phone,
      status: users[index].status,
      roles: users[index].roles,
      createdAt: users[index].createdAt,
      updatedAt: users[index].updatedAt
    }
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = users.findIndex(u => u.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '用户不存在' });
  }

  users.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

module.exports = router;
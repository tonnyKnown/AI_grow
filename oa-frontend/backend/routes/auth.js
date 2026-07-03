const express = require('express');
const bcrypt = require('bcryptjs');
const { users } = require('../data/mockData');
const { generateToken, verifyToken } = require('../middleware/auth');

const router = express.Router();

router.post('/login', (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ code: 400, message: '用户名和密码不能为空' });
  }

  const user = users.find(u => u.username === username);
  if (!user) {
    return res.status(401).json({ code: 401, message: '用户名或密码错误' });
  }

  if (!bcrypt.compareSync(password, user.password)) {
    return res.status(401).json({ code: 401, message: '用户名或密码错误' });
  }

  const token = generateToken(user);
  res.json({
    code: 200,
    message: '登录成功',
    data: {
      id: user.id,
      username: user.username,
      realName: user.realName,
      email: user.email,
      phone: user.phone,
      roles: user.roles,
      permissions: user.permissions,
      token
    }
  });
});

router.post('/logout', verifyToken, (req, res) => {
  res.json({ code: 200, message: '退出成功' });
});

router.get('/current', verifyToken, (req, res) => {
  const user = req.user;
  res.json({
    code: 200,
    data: {
      id: user.id,
      username: user.username,
      realName: user.realName,
      email: user.email,
      phone: user.phone,
      roles: user.roles,
      permissions: user.permissions
    }
  });
});

module.exports = router;
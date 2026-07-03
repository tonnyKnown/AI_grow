const jwt = require('jsonwebtoken');
const { users } = require('../data/mockData');

const SECRET_KEY = 'oa_system_secret_key_2026';

const generateToken = (user) => {
  return jwt.sign(
    { id: user.id, username: user.username, roles: user.roles },
    SECRET_KEY,
    { expiresIn: '8h' }
  );
};

const verifyToken = (req, res, next) => {
  const token = req.headers['authorization'];
  if (!token) {
    return res.status(401).json({ code: 401, message: '未登录，请先登录' });
  }

  jwt.verify(token.replace('Bearer ', ''), SECRET_KEY, (err, decoded) => {
    if (err) {
      return res.status(401).json({ code: 401, message: 'token失效，请重新登录' });
    }

    const user = users.find(u => u.id === decoded.id);
    if (!user) {
      return res.status(401).json({ code: 401, message: '用户不存在' });
    }

    req.user = user;
    next();
  });
};

module.exports = {
  generateToken,
  verifyToken
};
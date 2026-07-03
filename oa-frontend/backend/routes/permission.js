const express = require('express');
const { permissions } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  res.json({
    code: 200,
    data: permissions
  });
});

router.get('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const permission = permissions.find(p => p.id === parseInt(id));

  if (!permission) {
    return res.status(404).json({ code: 404, message: '权限不存在' });
  }

  res.json({
    code: 200,
    data: permission
  });
});

router.post('/', verifyToken, (req, res) => {
  const { permissionName, permissionDesc } = req.body;

  if (!permissionName) {
    return res.status(400).json({ code: 400, message: '权限名称不能为空' });
  }

  const exists = permissions.find(p => p.permissionName === permissionName);
  if (exists) {
    return res.status(400).json({ code: 400, message: '权限名称已存在' });
  }

  const newPermission = {
    id: permissions.length + 1,
    permissionName,
    permissionDesc: permissionDesc || ''
  };

  permissions.push(newPermission);

  res.json({
    code: 200,
    message: '创建成功',
    data: newPermission
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { permissionName, permissionDesc } = req.body;

  const index = permissions.findIndex(p => p.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '权限不存在' });
  }

  permissions[index] = {
    ...permissions[index],
    permissionName: permissionName || permissions[index].permissionName,
    permissionDesc: permissionDesc || permissions[index].permissionDesc
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: permissions[index]
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = permissions.findIndex(p => p.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '权限不存在' });
  }

  permissions.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

module.exports = router;
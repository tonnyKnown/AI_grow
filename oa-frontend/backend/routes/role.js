const express = require('express');
const { roles, permissions } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  const { roleName, pageNum = 1, pageSize = 10 } = req.query;

  let data = [...roles];

  if (roleName) {
    data = data.filter(item => item.roleName.includes(roleName));
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
  const role = roles.find(r => r.id === parseInt(id));

  if (!role) {
    return res.status(404).json({ code: 404, message: '角色不存在' });
  }

  res.json({
    code: 200,
    data: role
  });
});

router.post('/', verifyToken, (req, res) => {
  const { roleName, roleDesc, permissions = [] } = req.body;

  if (!roleName) {
    return res.status(400).json({ code: 400, message: '角色名称不能为空' });
  }

  const exists = roles.find(r => r.roleName === roleName);
  if (exists) {
    return res.status(400).json({ code: 400, message: '角色名称已存在' });
  }

  const now = new Date().toLocaleString();
  const newRole = {
    id: Date.now(),
    roleName,
    roleDesc: roleDesc || '',
    permissions: Array.isArray(permissions) ? permissions : [],
    createdAt: now,
    updatedAt: now
  };

  roles.push(newRole);

  res.json({
    code: 200,
    message: '创建成功',
    data: newRole
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { roleName, roleDesc, permissions } = req.body;

  const index = roles.findIndex(r => r.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '角色不存在' });
  }

  roles[index] = {
    ...roles[index],
    roleName: roleName || roles[index].roleName,
    roleDesc: roleDesc || roles[index].roleDesc,
    permissions: permissions !== undefined ? (Array.isArray(permissions) ? permissions : roles[index].permissions) : roles[index].permissions,
    updatedAt: new Date().toLocaleString()
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: roles[index]
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = roles.findIndex(r => r.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '角色不存在' });
  }

  roles.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

router.get('/:id/permissions', verifyToken, (req, res) => {
  const { id } = req.params;
  const role = roles.find(r => r.id === parseInt(id));

  if (!role) {
    return res.status(404).json({ code: 404, message: '角色不存在' });
  }

  res.json({
    code: 200,
    data: role.permissions || []
  });
});

router.get('/permissions/tree', verifyToken, (req, res) => {
  const tree = permissions.map(p => ({
    id: p.id,
    label: p.permissionDesc,
    value: p.permissionName
  }));

  res.json({
    code: 200,
    data: tree
  });
});

router.post('/:id/permissions', verifyToken, (req, res) => {
  const { id } = req.params;
  const permissionIds = req.body;

  const index = roles.findIndex(r => r.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '角色不存在' });
  }

  const permissionNames = permissions
    .filter(p => permissionIds.includes(p.id))
    .map(p => p.permissionName);

  roles[index].permissions = permissionNames;
  roles[index].updatedAt = new Date().toLocaleString();

  res.json({
    code: 200,
    message: '权限分配成功',
    data: roles[index]
  });
});

router.put('/', verifyToken, (req, res) => {
  const { id, roleName, roleDesc, permissions } = req.body;

  const index = roles.findIndex(r => r.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '角色不存在' });
  }

  roles[index] = {
    ...roles[index],
    roleName: roleName || roles[index].roleName,
    roleDesc: roleDesc || roles[index].roleDesc,
    permissions: permissions !== undefined ? (Array.isArray(permissions) ? permissions : roles[index].permissions) : roles[index].permissions,
    updatedAt: new Date().toLocaleString()
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: roles[index]
  });
});

module.exports = router;
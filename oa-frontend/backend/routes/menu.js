const express = require('express');
const { menus } = require('../data/mockData');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  res.json({
    code: 200,
    data: menus
  });
});

router.get('/list', verifyToken, (req, res) => {
  res.json({
    code: 200,
    data: menus
  });
});

router.get('/roles', verifyToken, (req, res) => {
  const { roles } = req.query;
  if (!roles) {
    return res.json({
      code: 200,
      data: menus
    });
  }
  
  const roleList = roles.split(',');
  const accessibleMenus = menus.filter(menu => {
    return true;
  });
  
  res.json({
    code: 200,
    data: accessibleMenus
  });
});

router.get('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const menu = menus.find(m => m.id === parseInt(id));

  if (!menu) {
    return res.status(404).json({ code: 404, message: '菜单不存在' });
  }

  res.json({
    code: 200,
    data: menu
  });
});

router.post('/', verifyToken, (req, res) => {
  const { menuName, path, component, icon, parentId = null, orderNum = 0, roleKeys = '', status = 1, remark = '' } = req.body;

  if (!menuName || !path) {
    return res.status(400).json({ code: 400, message: '菜单名称和路径不能为空' });
  }

  const createdBy = req.body.createdBy || req.user.username || 'admin';

  const newMenu = {
    id: Date.now(),
    menuName,
    path,
    component: component || '',
    icon: icon || '',
    parentId: parentId !== null ? parseInt(parentId) : null,
    orderNum: parseInt(orderNum),
    roleKeys,
    status: parseInt(status),
    remark,
    createdBy,
    createdAt: new Date().toLocaleString(),
    updatedAt: new Date().toLocaleString()
  };

  menus.push(newMenu);

  res.json({
    code: 200,
    message: '创建成功',
    data: newMenu
  });
});

router.put('/:id', verifyToken, (req, res) => {
  const { id } = req.params;
  const { menuName, path, component, icon, parentId, orderNum, roleKeys, status, remark } = req.body;

  const index = menus.findIndex(m => m.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '菜单不存在' });
  }

  menus[index] = {
    ...menus[index],
    menuName: menuName !== undefined ? menuName : menus[index].menuName,
    path: path !== undefined ? path : menus[index].path,
    component: component !== undefined ? component : menus[index].component,
    icon: icon !== undefined ? icon : menus[index].icon,
    parentId: parentId !== undefined ? (parentId === null ? null : parseInt(parentId)) : menus[index].parentId,
    orderNum: orderNum !== undefined ? parseInt(orderNum) : menus[index].orderNum,
    roleKeys: roleKeys !== undefined ? roleKeys : menus[index].roleKeys,
    status: status !== undefined ? parseInt(status) : menus[index].status,
    remark: remark !== undefined ? remark : menus[index].remark,
    updatedAt: new Date().toLocaleString()
  };

  res.json({
    code: 200,
    message: '修改成功',
    data: menus[index]
  });
});

router.delete('/:id', verifyToken, (req, res) => {
  const { id } = req.params;

  const index = menus.findIndex(m => m.id === parseInt(id));
  if (index === -1) {
    return res.status(404).json({ code: 404, message: '菜单不存在' });
  }

  menus.splice(index, 1);

  res.json({
    code: 200,
    message: '删除成功'
  });
});

module.exports = router;
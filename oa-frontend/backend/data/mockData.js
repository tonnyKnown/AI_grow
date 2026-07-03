const bcrypt = require('bcryptjs');

const users = [
  {
    id: 1,
    username: 'admin',
    password: bcrypt.hashSync('123456', 10),
    realName: '管理员',
    email: 'admin@example.com',
    phone: '13800138000',
    status: 1,
    roles: ['admin'],
    permissions: ['user:manage', 'role:manage', 'permission:manage', 'product:manage', 'marketing:manage', 'order:manage', 'menu:manage'],
    createdAt: '2026-01-01 00:00:00',
    updatedAt: '2026-01-01 00:00:00'
  },
  {
    id: 2,
    username: 'user',
    password: bcrypt.hashSync('123456', 10),
    realName: '普通用户',
    email: 'user@example.com',
    phone: '13800138001',
    status: 1,
    roles: ['user'],
    permissions: ['product:view', 'order:view'],
    createdAt: '2026-01-02 00:00:00',
    updatedAt: '2026-01-02 00:00:00'
  }
];

const roles = [
  {
    id: 1,
    roleName: 'admin',
    roleDesc: '超级管理员',
    permissions: ['user:manage', 'role:manage', 'permission:manage', 'product:manage', 'marketing:manage', 'order:manage', 'menu:manage'],
    createdAt: '2026-01-01 00:00:00',
    updatedAt: '2026-01-01 00:00:00'
  },
  {
    id: 2,
    roleName: 'user',
    roleDesc: '普通用户',
    permissions: ['product:view', 'order:view'],
    createdAt: '2026-01-01 00:00:00',
    updatedAt: '2026-01-01 00:00:00'
  }
];

const permissions = [
  { id: 1, permissionName: 'user:manage', permissionDesc: '用户管理' },
  { id: 2, permissionName: 'role:manage', permissionDesc: '角色管理' },
  { id: 3, permissionName: 'permission:manage', permissionDesc: '权限管理' },
  { id: 4, permissionName: 'product:manage', permissionDesc: '商品管理' },
  { id: 5, permissionName: 'marketing:manage', permissionDesc: '营销推广管理' },
  { id: 6, permissionName: 'order:manage', permissionDesc: '订单管理' },
  { id: 7, permissionName: 'menu:manage', permissionDesc: '菜单管理' },
  { id: 8, permissionName: 'product:view', permissionDesc: '商品查看' },
  { id: 9, permissionName: 'order:view', permissionDesc: '订单查看' }
];

const menus = [
  { id: 1, menuName: '首页', path: '/dashboard', parentId: 0, sort: 1 },
  { id: 2, menuName: '用户管理', path: '/users', parentId: 0, sort: 2 },
  { id: 3, menuName: '角色管理', path: '/roles', parentId: 0, sort: 3 },
  { id: 4, menuName: '权限管理', path: '/permissions', parentId: 0, sort: 4 },
  { id: 5, menuName: '商品管理', path: '/products', parentId: 0, sort: 5 },
  { id: 6, menuName: '营销推广', path: '/marketing', parentId: 0, sort: 6 },
  { id: 7, menuName: '订单管理', path: '/orders', parentId: 0, sort: 7 },
  { id: 8, menuName: '菜单管理', path: '/menus', parentId: 0, sort: 8 }
];

const products = [
  { id: 1, name: '华为Mate60 Pro', category: '手机', price: 6999, stock: 100, status: 1, createdAt: '2026-01-01 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 2, name: 'iPhone 15 Pro', category: '手机', price: 7999, stock: 80, status: 1, createdAt: '2026-01-02 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 3, name: '小米14', category: '手机', price: 4999, stock: 150, status: 1, createdAt: '2026-01-03 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 4, name: 'MacBook Pro 14', category: '电脑', price: 14999, stock: 50, status: 1, createdAt: '2026-01-04 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 5, name: 'ThinkPad X1', category: '电脑', price: 12999, stock: 30, status: 1, createdAt: '2026-01-05 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 6, name: 'AirPods Pro 2', category: '配件', price: 1899, stock: 200, status: 1, createdAt: '2026-01-06 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 7, name: 'Apple Watch Ultra', category: '配件', price: 6299, stock: 40, status: 1, createdAt: '2026-01-07 10:00:00', updatedAt: '2026-05-10 10:00:00' },
  { id: 8, name: '索尼WH-1000XM5', category: '配件', price: 2999, stock: 60, status: 1, createdAt: '2026-01-08 10:00:00', updatedAt: '2026-05-10 10:00:00' }
];

const orders = [
  { id: 1, orderNo: 'ORD20260501001', productName: '华为Mate60 Pro', quantity: 1, totalPrice: 6999, status: 'completed', userId: 1, createdAt: '2026-05-01 10:00:00' },
  { id: 2, orderNo: 'ORD20260502002', productName: 'iPhone 15 Pro', quantity: 1, totalPrice: 7999, status: 'completed', userId: 2, createdAt: '2026-05-02 14:00:00' },
  { id: 3, orderNo: 'ORD20260503003', productName: '小米14', quantity: 2, totalPrice: 9998, status: 'pending', userId: 1, createdAt: '2026-05-03 09:00:00' },
  { id: 4, orderNo: 'ORD20260504004', productName: 'MacBook Pro 14', quantity: 1, totalPrice: 14999, status: 'processing', userId: 2, createdAt: '2026-05-04 16:00:00' },
  { id: 5, orderNo: 'ORD20260505005', productName: 'AirPods Pro 2', quantity: 1, totalPrice: 1899, status: 'completed', userId: 1, createdAt: '2026-05-05 11:00:00' },
  { id: 6, orderNo: 'ORD20260506006', productName: 'ThinkPad X1', quantity: 1, totalPrice: 12999, status: 'pending', userId: 2, createdAt: '2026-05-06 15:00:00' },
  { id: 7, orderNo: 'ORD20260507007', productName: '索尼WH-1000XM5', quantity: 1, totalPrice: 2999, status: 'completed', userId: 1, createdAt: '2026-05-07 10:00:00' },
  { id: 8, orderNo: 'ORD20260508008', productName: 'Apple Watch Ultra', quantity: 1, totalPrice: 6299, status: 'processing', userId: 2, createdAt: '2026-05-08 14:00:00' },
  { id: 9, orderNo: 'ORD20260509009', productName: '华为Mate60 Pro', quantity: 1, totalPrice: 6999, status: 'pending', userId: 1, createdAt: '2026-05-09 09:00:00' },
  { id: 10, orderNo: 'ORD20260510010', productName: '小米14', quantity: 1, totalPrice: 4999, status: 'completed', userId: 2, createdAt: '2026-05-10 10:00:00' }
];

const marketingActivities = [
  {
    id: 1,
    name: '618年中大促',
    type: 'discount',
    discount: 8.5,
    startTime: '2026-06-18 00:00:00',
    endTime: '2026-06-20 23:59:59',
    status: 'pending',
    description: '年中大促活动，全场商品85折优惠',
    createdAt: '2026-06-01 10:00:00',
    updatedAt: '2026-06-01 10:00:00'
  },
  {
    id: 2,
    name: '限时秒杀专场',
    type: 'flash',
    discount: 5.0,
    startTime: '2026-05-15 10:00:00',
    endTime: '2026-05-15 12:00:00',
    status: 'ended',
    description: '限时2小时秒杀活动',
    createdAt: '2026-05-10 14:00:00',
    updatedAt: '2026-05-10 14:00:00'
  },
  {
    id: 3,
    name: '三人团购优惠',
    type: 'group',
    discount: 7.0,
    startTime: '2026-05-20 00:00:00',
    endTime: '2026-05-25 23:59:59',
    status: 'pending',
    description: '三人成团享7折优惠',
    createdAt: '2026-05-10 16:00:00',
    updatedAt: '2026-05-10 16:00:00'
  },
  {
    id: 4,
    name: '新人专享优惠券',
    type: 'coupon',
    discount: 9.0,
    startTime: '2026-01-01 00:00:00',
    endTime: '2026-12-31 23:59:59',
    status: 'active',
    description: '新用户注册即可领取9折优惠券',
    createdAt: '2025-12-20 09:00:00',
    updatedAt: '2025-12-20 09:00:00'
  },
  {
    id: 5,
    name: '双11狂欢节',
    type: 'discount',
    discount: 7.5,
    startTime: '2026-11-11 00:00:00',
    endTime: '2026-11-12 23:59:59',
    status: 'pending',
    description: '双11全场75折优惠',
    createdAt: '2026-05-10 11:00:00',
    updatedAt: '2026-05-10 11:00:00'
  }
];

module.exports = {
  users,
  roles,
  permissions,
  menus,
  products,
  orders,
  marketingActivities
};
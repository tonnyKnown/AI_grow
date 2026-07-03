const axios = require('axios');

const API_BASE = 'http://localhost:8081/api';

// 测试 logout 功能
async function testLogout() {
  try {
    console.log('1. 测试登录...');
    const loginRes = await axios.post(`${API_BASE}/system/auth/login`, {
      username: 'admin',
      password: '123456'
    });
    console.log('登录成功:', loginRes.data.message);
    const token = loginRes.data.data.token;
    console.log('Token:', token);

    console.log('\n2. 测试登出...');
    const logoutRes = await axios.post(
      `${API_BASE}/system/auth/logout`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
    console.log('登出成功:', logoutRes.data.message);

    console.log('\n✓ logout 接口测试通过');
  } catch (error) {
    console.error('测试失败:', error.response?.data || error.message);
  }
}

testLogout();

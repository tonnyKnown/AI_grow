import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        handleAuthError()
      }
      if (res.code === 503) {
        handleServiceUnavailable()
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      handleAuthError()
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限访问该资源')
    } else if (error.response?.status === 502 || error.response?.status === 503) {
      handleServiceUnavailable()
    } else if (error.code === 'ECONNABORTED') {
      handleServiceUnavailable()
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

function handleAuthError() {
  if (router.currentRoute.value?.path === '/login') {
    return
  }
  localStorage.removeItem('token')
  ElMessage.error('登录已过期，请重新登录')
  router.push('/login')
}

function handleServiceUnavailable() {
  if (router.currentRoute.value?.path === '/maintenance') {
    return
  }
  router.push('/maintenance')
}

export default request

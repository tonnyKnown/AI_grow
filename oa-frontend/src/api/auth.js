import request from '@/utils/request'

export const login = (data) => {
  return request.post('/system/auth/login', data)
}

export const logout = () => {
  return request.post('/system/auth/logout')
}

export const getCurrentUser = () => {
  return request.get('/system/auth/current')
}

export const getMenu = (roles) => {
  if (roles && roles.length > 0) {
    return request.get('/system/menu/roles', { params: { roles: roles.join(',') } })
  }
  return request.get('/system/menu/tree')
}

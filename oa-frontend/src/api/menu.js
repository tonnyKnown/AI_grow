import request from '@/utils/request'

export const getMenuPage = () => {
  return request.get('/system/menu/list')
}

export const getMenuById = (id) => {
  return request.get(`/system/menu/${id}`)
}

export const getAllMenus = () => {
  return request.get('/system/menu/list')
}

export const getMenuTree = () => {
  return request.get('/system/menu/list')
}

export const getMenusByRoles = (roles) => {
  if (roles && roles.length > 0) {
    return request.get('/system/menu/roles', { params: { roles: roles.join(',') } })
  }
  return request.get('/system/menu/list')
}

export const createMenu = (data) => {
  return request.post('/system/menu', data)
}

export const updateMenu = (id, data) => {
  return request.put(`/system/menu/${id}`, data)
}

export const deleteMenu = (id) => {
  return request.delete(`/system/menu/${id}`)
}

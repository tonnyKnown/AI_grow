import request from '@/utils/request'

export const getPermissionPage = (params) => {
  return request.get('/system/permissions', { params })
}

export const getPermissionById = (id) => {
  return request.get(`/system/permissions/${id}`)
}

export const getAllPermissions = () => {
  return request.get('/system/permissions/all')
}

export const getMenuTree = () => {
  return request.get('/system/permissions/menu')
}

export const createPermission = (data) => {
  return request.post('/system/permissions', data)
}

export const updatePermission = (data) => {
  return request.put('/system/permissions', data)
}

export const deletePermission = (id) => {
  return request.delete(`/system/permissions/${id}`)
}

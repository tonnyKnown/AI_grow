import request from '@/utils/request'

export const getRolePage = (params) => {
  return request.get('/system/roles', { params })
}

export const getRoleById = (id) => {
  return request.get(`/system/roles/${id}`)
}

export const getRolePermissionIds = (id) => {
  return request.get(`/system/roles/${id}/permissions`)
}

export const getPermissionTree = () => {
  return request.get('/system/roles/permissions/tree')
}

export const createRole = (data) => {
  return request.post('/system/roles', data)
}

export const updateRole = (data) => {
  return request.put('/system/roles', data)
}

export const deleteRole = (id) => {
  return request.delete(`/system/roles/${id}`)
}

export const assignPermissions = (id, permissionIds) => {
  return request.post(`/system/roles/${id}/permissions`, permissionIds)
}

export const getRoleList = () => {
  return request.get('/system/roles', { params: { pageNum: 1, pageSize: 100 } })
}

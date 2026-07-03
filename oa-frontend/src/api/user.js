import request from '@/utils/request'

export const getUserPage = (params) => {
  return request.get('/system/users', { params })
}

export const getUserById = (id) => {
  return request.get(`/system/users/${id}`)
}

export const createUser = (data) => {
  return request.post('/system/users', data)
}

export const updateUser = (data) => {
  return request.put('/system/users', data)
}

export const deleteUser = (id) => {
  return request.delete(`/system/users/${id}`)
}

export const updatePassword = (id, oldPassword, newPassword) => {
  return request.put('/system/users/password', null, {
    params: { id, oldPassword, newPassword }
  })
}

export const assignRoles = (id, roleIds) => {
  return request.post(`/system/users/${id}/roles`, roleIds)
}

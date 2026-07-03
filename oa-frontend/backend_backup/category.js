import request from '@/utils/request'

export const getCategoryPage = (params) => {
  return request.get('/categories', { params })
}

export const getAllCategories = () => {
  return request.get('/categories/all')
}

export const getCategoryById = (id) => {
  return request.get(`/categories/${id}`)
}

export const createCategory = (data) => {
  return request.post('/categories', data)
}

export const updateCategory = (data) => {
  return request.put('/categories', data)
}

export const deleteCategory = (id) => {
  return request.delete(`/categories/${id}`)
}

import request from '@/utils/request'

export const getProductPage = (params) => {
  return request.get('/business/products', { params })
}

export const getProductById = (id) => {
  return request.get(`/business/products/${id}`)
}

export const createProduct = (data) => {
  return request.post('/business/products', data)
}

export const updateProduct = (data) => {
  return request.put('/business/products', data)
}

export const deleteProduct = (id) => {
  return request.delete(`/business/products/${id}`)
}

export const updateStock = (id, quantity) => {
  return request.put(`/business/products/${id}/stock`, null, { params: { quantity } })
}

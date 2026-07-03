import request from '@/utils/request'

export const getMarketingList = (params) => {
  return request.get('/business/marketing', { params })
}

export const getAllMarketing = () => {
  return request.get('/business/marketing/list')
}

export const getMarketingById = (id) => {
  return request.get(`/business/marketing/${id}`)
}

export const createMarketing = (data) => {
  return request.post('/business/marketing', data)
}

export const updateMarketing = (data) => {
  return request.put('/business/marketing', data)
}

export const deleteMarketing = (id) => {
  return request.delete(`/business/marketing/${id}`)
}

export const getMarketingByType = (type) => {
  return request.get(`/business/marketing/type/${type}`)
}

import request from '@/utils/request'

export const getOrderPage = (params) => {
  return request({
    url: '/business/orders',
    method: 'get',
    params
  })
}

export const getOrderById = (id) => {
  return request({
    url: `/business/orders/${id}`,
    method: 'get'
  })
}

export const createOrder = (data) => {
  return request({
    url: '/business/orders',
    method: 'post',
    data
  })
}

export const updateOrder = (data) => {
  return request({
    url: '/business/orders',
    method: 'put',
    data
  })
}

export const updateOrderStatus = (id, status) => {
  return request({
    url: '/business/orders',
    method: 'put',
    data: { id, status }
  })
}

export const deleteOrder = (id) => {
  return request({
    url: `/business/orders/${id}`,
    method: 'delete'
  })
}

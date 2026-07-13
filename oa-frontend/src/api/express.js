import request from '@/utils/request'

export const getExpressByOrderId = (orderId) => {
  return request({
    url: `/business/express/order/${orderId}`,
    method: 'get'
  })
}

export const getExpressList = (params) => {
  return request({
    url: '/business/express/list',
    method: 'get',
    params
  })
}

export const createExpress = (data) => {
  return request({
    url: '/business/express',
    method: 'post',
    data
  })
}

export const updateExpress = (data) => {
  return request({
    url: '/business/express',
    method: 'put',
    data
  })
}

export const updateExpressTracking = (id, trackingNodes) => {
  return request({
    url: '/business/express/tracking',
    method: 'put',
    data: { id, trackingNodes }
  })
}

export const deleteExpress = (id) => {
  return request({
    url: `/business/express/${id}`,
    method: 'delete'
  })
}

export const queryRealTimeExpress = (id) => {
  return request({
    url: `/business/express/query/${id}`,
    method: 'get'
  })
}

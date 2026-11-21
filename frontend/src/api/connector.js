import request from '@/utils/request'

export function listConnectors({ page = 1, size = 100 } = {}) {
  return request.get('/connectors', { params: { page, size } })
}

export function getConnector(id) {
  return request.get(`/connectors/${id}`)
}

/** 创建连接器：{ name, description, serverType, endpoint } */
export function createConnector(data) {
  return request.post('/connectors', data)
}

/** 更新连接器：字段 + 可选 status（1 启用 / 0 停用） */
export function updateConnector(id, data) {
  return request.put(`/connectors/${id}`, data)
}

export function deleteConnector(id) {
  return request.delete(`/connectors/${id}`)
}

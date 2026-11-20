import request from '@/utils/request'

export function listDevices({ page = 1, size = 100 } = {}) {
  return request.get('/devices', { params: { page, size } })
}

/** 登记设备：{ name, os, browser, ip, location } */
export function createDevice(data) {
  return request.post('/devices', data)
}

/** 仅允许重命名：{ name } */
export function updateDevice(id, data) {
  return request.put(`/devices/${id}`, data)
}

export function deleteDevice(id) {
  return request.delete(`/devices/${id}`)
}

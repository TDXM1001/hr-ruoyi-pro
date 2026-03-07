import request from '@/utils/http'

// 查询字典类型列表
export function listType(query?: any) {
  return request.get({ url: '/system/dict/type/list', params: query })
}

// 查询字典类型详细
export function getType(dictId: number | string) {
  return request.get({ url: '/system/dict/type/' + dictId })
}

// 新增字典类型
export function addType(data: any) {
  return request.post({ url: '/system/dict/type', data: data })
}

// 修改字典类型
export function updateType(data: any) {
  return request.put({ url: '/system/dict/type', data: data })
}

// 删除字典类型
export function delType(dictId: number | string) {
  return request.del({ url: '/system/dict/type/' + dictId })
}

// 刷新字典缓存
export function refreshCache() {
  return request.del({ url: '/system/dict/type/refreshCache' })
}

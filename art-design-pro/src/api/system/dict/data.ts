import request from '@/utils/http'

// 查询字典数据列表
export function listData(query?: any) {
  return request.get({ url: '/system/dict/data/list', params: query })
}

// 查询字典数据详细
export function getData(dictCode: number | string) {
  return request.get({ url: '/system/dict/data/' + dictCode })
}

// 根据字典类型查询字典数据信息
export function getDicts(dictType: string) {
  return request.get({ url: '/system/dict/data/type/' + dictType })
}

// 新增字典数据
export function addData(data: any) {
  return request.post({ url: '/system/dict/data', data: data })
}

// 修改字典数据
export function updateData(data: any) {
  return request.put({ url: '/system/dict/data', data: data })
}

// 删除字典数据
export function delData(dictCode: number | string) {
  return request.del({ url: '/system/dict/data/' + dictCode })
}

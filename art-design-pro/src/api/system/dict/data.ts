import request from '@/utils/http/index'

// 根据字典类型查询字典数据信息
export function getDicts(dictType: string) {
  return request.get<any[]>({
    url: '/system/dict/data/type/' + dictType
  })
}

import request from '@/utils/http'
import type { AssetAggregateDetail, AssetAggregateReq, AssetListItem } from '@/types/asset'

/**
 * 查询资产信息列表
 * @param params 查询参数
 */
export function listInfo(params?: any) {
  return request.get<AssetListItem[]>({
    url: '/asset/info/list',
    params
  })
}

/**
 * 获取资产信息详细信息
 * @param assetId 资产主键
 */
export function getInfo(assetId: number | string) {
  return request.get<AssetAggregateDetail>({
    url: `/asset/info/${assetId}`
  })
}

/**
 * 新增资产聚合信息
 * @param data 聚合请求体
 */
export function addInfo(data: AssetAggregateReq) {
  return request.post({
    url: '/asset/info',
    data
  })
}

/**
 * 修改资产聚合信息
 * @param data 聚合请求体
 */
export function updateInfo(data: AssetAggregateReq) {
  return request.put({
    url: '/asset/info',
    data
  })
}

/**
 * 删除资产信息
 * @param assetId 资产主键
 */
export function delInfo(assetId: number | string) {
  return request.del({
    url: `/asset/info/${assetId}`
  })
}

/**
 * 导出资产信息
 * @param params 查询参数
 */
export function exportInfo(params?: any) {
  return request.post({
    url: '/asset/info/export',
    params,
    responseType: 'blob'
  })
}

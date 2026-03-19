import http from '@/utils/http'

/**
 * 资产交接主单查询参数。
 */
export interface AssetHandoverOrderQuery {
  handoverNo?: string
  handoverType?: string
  handoverStatus?: string
  assetType?: string
  toDeptId?: number
  toUserId?: number
  'params[beginTime]'?: string
  'params[endTime]'?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 资产交接建单参数。
 */
export interface AssetHandoverCreatePayload {
  handoverType: 'ASSIGN' | 'TRANSFER' | 'RETURN'
  handoverDate: string
  assetIds: number[]
  toDeptId?: number
  toUserId?: number
  locationName?: string
  remark?: string
}

/**
 * 查询资产交接主单列表。
 * @param query 查询条件
 * @returns 主单分页数据
 */
export function listAssetHandoverOrder(query?: AssetHandoverOrderQuery) {
  return http.request({ url: '/asset/handover/order/list', method: 'get', params: query })
}

/**
 * 查询交接主单明细。
 * @param handoverOrderId 交接主单ID
 * @returns 主单明细列表
 */
export function listAssetHandoverItems(handoverOrderId: number | string) {
  return http.request({ url: `/asset/handover/order/${handoverOrderId}/items`, method: 'get' })
}

/**
 * 新增资产交接主单。
 * @param data 建单参数
 * @returns 新增结果（交接主单ID）
 */
export function addAssetHandoverOrder(data: AssetHandoverCreatePayload) {
  return http.request({ url: '/asset/handover/order', method: 'post', data })
}

/**
 * 兼容任务文档中的命名：查询交接列表。
 */
export const listAssetHandover = listAssetHandoverOrder

/**
 * 兼容任务文档中的命名：新增交接主单。
 */
export const addAssetHandover = addAssetHandoverOrder

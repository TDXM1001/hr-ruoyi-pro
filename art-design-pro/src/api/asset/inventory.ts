import http from '@/utils/http'

/**
 * 盘点任务查询参数
 */
export interface AssetInventoryTaskQuery {
  taskId?: number
  taskNo?: string
  taskName?: string
  taskStatus?: string
  scopeType?: string
  'params[beginTime]'?: string
  'params[endTime]'?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 盘点任务创建参数
 */
export interface AssetInventoryTaskPayload {
  taskName: string
  assetIds: number[]
  plannedDate: string
  remark?: string
}

/**
 * 盘点结果提报参数
 */
export interface AssetInventoryResultPayload {
  taskId: number
  assetId: number
  inventoryResult: 'NORMAL' | 'LOSS' | 'MISSING' | 'DAMAGED' | string
  followUpAction?: 'NONE' | 'UPDATE_LEDGER' | 'CREATE_DISPOSAL' | string
  actualLocationName?: string
  actualUseDeptId?: number
  actualResponsibleUserId?: number
  confirmedUse?: boolean
  checkedTime?: string
  resultDesc?: string
  remark?: string
}

/**
 * 盘点任务资产明细查询参数
 */
export interface AssetInventoryTaskAssetQuery {
  assetId?: number
  assetCode?: string
  assetName?: string
  assetStatus?: string
  resultType?: 'ALL' | 'PENDING' | 'REGISTERED' | 'ABNORMAL' | string
  pageNum?: number
  pageSize?: number
}

/**
 * 查询盘点任务列表
 * @param params 查询参数
 * @returns 盘点任务分页列表
 */
export function listInventoryTask(params?: AssetInventoryTaskQuery) {
  return http.request({ url: '/asset/inventory/task/list', method: 'get', params })
}

/**
 * 查询盘点任务详情
 * @param taskId 任务ID
 * @returns 盘点任务详情
 */
export function getInventoryTask(taskId: number | string) {
  return http.request({ url: `/asset/inventory/task/${taskId}`, method: 'get' })
}

/**
 * 查询盘点任务资产明细列表
 * @param taskId 任务ID
 * @param params 查询参数
 * @returns 任务资产分页列表
 */
export function listInventoryTaskAssets(
  taskId: number | string,
  params?: AssetInventoryTaskAssetQuery
) {
  return http.request({ url: `/asset/inventory/task/${taskId}/assets`, method: 'get', params })
}

/**
 * 新增盘点任务
 * @param data 新增参数
 * @returns 新增后的任务ID
 */
export function addInventoryTask(data: AssetInventoryTaskPayload) {
  return http.request({ url: '/asset/inventory/task', method: 'post', data })
}

/**
 * 提交盘点结果
 * @param data 盘点结果参数
 * @returns 提交结果
 */
export function submitInventoryResult(data: AssetInventoryResultPayload) {
  return http.request({ url: '/asset/inventory/result', method: 'post', data })
}

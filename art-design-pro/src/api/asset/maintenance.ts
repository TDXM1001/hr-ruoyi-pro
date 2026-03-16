import type { AssetBusinessOrderBase } from '@/types/asset'
import request from '@/utils/http'

/**
 * 维修台账列表查询参数
 */
export interface MaintenanceQuery {
  maintenanceNo?: string
  assetNo?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 发起维修申请请求
 *
 * 统一要求显式携带 `assetId` 与 `assetNo`，
 * 避免不同入口继续依赖后端按编号反查主档。
 */
export interface ApplyMaintenanceReq {
  assetId: number
  assetNo: string
  reason: string
}

/**
 * 维修台账最小行结构。
 */
export interface AssetMaintenanceItem extends AssetBusinessOrderBase {
  maintenanceNo: string
  assetId: number
  assetNo: string
  reason?: string
  status: number
  wfStatus?: string
  applyUserId?: number
  createTime?: string
}

/**
 * 查询维修记录列表
 */
export function listMaintenance(params?: MaintenanceQuery) {
  return request.get<AssetMaintenanceItem[]>({
    url: '/asset/maintenance/list',
    params
  })
}

/**
 * 查询维修详情
 */
export function getMaintenance(maintenanceNo: string) {
  return request.get<AssetMaintenanceItem>({
    url: `/asset/maintenance/${maintenanceNo}`
  })
}

/**
 * 提交维修申请
 */
export function applyMaintenance(data: ApplyMaintenanceReq) {
  return request.post({
    url: '/asset/maintenance',
    data
  })
}

/**
 * 完成维修
 */
export function completeMaintenance(maintenanceNo: string) {
  return request.post({
    url: `/asset/maintenance/complete/${maintenanceNo}`
  })
}

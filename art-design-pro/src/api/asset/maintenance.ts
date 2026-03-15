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
 * 资产列表跳转场景会优先传 assetId，
 * 菜单直接进入台账页时则允许只传 assetNo，由后端兜底解析主档。
 */
export interface ApplyMaintenanceReq {
  assetId?: number
  assetNo: string
  reason: string
}

/**
 * 维修台账最小行结构
 */
export interface AssetMaintenanceItem {
  maintenanceNo: string
  assetId?: number
  assetNo: string
  reason?: string
  status: number
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

import http from '@/utils/http'

/**
 * 资产统计总览对象
 */
export interface AssetOverviewStats {
  totalCount: number
  inUseCount: number
  idleCount: number
  inventoryingCount: number
  pendingDisposalCount: number
  disposedCount: number
  overdueInventoryCount: number
}

/**
 * 查询资产统计总览
 * @returns 统计总览
 */
export function getAssetOverviewStats() {
  return http.request<AssetOverviewStats>({ url: '/asset/stats/overview', method: 'get' })
}

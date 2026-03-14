import request from '@/utils/http'
import type { AssetDepreciationLog, AssetFinanceInfo } from '@/types/asset'

/**
 * 按资产主键重算财务信息。
 */
export function recalculateFinance(assetId: number | string) {
  return request.post<AssetFinanceInfo>({
    url: `/asset/finance/${assetId}/recalculate`
  })
}

/**
 * 查询资产完整折旧日志。
 */
export function listDepreciationLogs(assetId: number | string) {
  return request.get<AssetDepreciationLog[]>({
    url: `/asset/finance/${assetId}/depreciation-logs`
  })
}

/**
 * 按期间批量计提折旧。
 */
export function accrueDepreciation(period: string) {
  return request.post({
    url: `/asset/finance/accrue/${period}`
  })
}

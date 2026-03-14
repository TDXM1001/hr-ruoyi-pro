import type { AssetListItem } from '@/types/asset'

/** 资产申请弹窗使用的最小上下文。 */
export interface AssetApplyContext {
  assetId: number
  assetNo: string
  assetName: string
  assetStatus: string
}

/** 构建资产台账查询参数，统一收敛状态字段口径。 */
export function buildAssetListQuery(filters: {
  assetNo?: string
  assetName?: string
  assetStatus?: string
  categoryId?: number
}) {
  return {
    assetNo: filters.assetNo?.trim() || undefined,
    assetName: filters.assetName?.trim() || undefined,
    assetStatus: filters.assetStatus || undefined,
    categoryId: filters.categoryId
  }
}

/** 从表格多选结果中提取资产主键集合。 */
export function collectAssetIds(selection: Array<Pick<AssetListItem, 'assetId'>>) {
  return selection.map((item) => item.assetId)
}

/** 把列表行转换成申请弹窗需要的资产上下文。 */
export function toApplyAssetContext(
  row: Pick<AssetListItem, 'assetId' | 'assetNo' | 'assetName' | 'assetStatus'>
): AssetApplyContext {
  return {
    assetId: row.assetId,
    assetNo: row.assetNo,
    assetName: row.assetName,
    assetStatus: row.assetStatus
  }
}

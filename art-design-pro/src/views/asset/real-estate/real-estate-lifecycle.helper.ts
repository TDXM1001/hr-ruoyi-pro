/** 不动产动作页从资产台账透传的最小上下文。 */
export interface RealEstateRouteAssetContext {
  assetId?: number
  assetNo: string
  assetName?: string
  assetStatus?: string
}

/** 搜索下拉使用的最小选项结构。 */
export interface RealEstateStatusOption {
  label: string
  value: string
}

/** 审批型不动产动作的单据状态选项。 */
export const REAL_ESTATE_APPROVAL_STATUS_OPTIONS: RealEstateStatusOption[] = [
  { label: '流转中', value: 'pending' },
  { label: '已完成', value: 'approved' },
  { label: '已驳回', value: 'rejected' }
]

/** 免审批不动产动作的单据状态选项。 */
export const REAL_ESTATE_DIRECT_STATUS_OPTIONS: RealEstateStatusOption[] = [
  { label: '已完成', value: 'completed' }
]

/**
 * 把资产列表 query 还原成动作页上下文。
 *
 * 这里故意做得宽松一些：只要带了 `assetNo`，台账页就能回显并允许直接建单。
 */
export function parseAssetRouteQuery(
  query: Record<string, unknown>
): RealEstateRouteAssetContext | null {
  const assetNo = String(query.assetNo || '').trim()
  if (!assetNo) {
    return null
  }

  const assetId = Number(query.assetId)
  return {
    assetId: Number.isNaN(assetId) ? undefined : assetId,
    assetNo,
    assetName: String(query.assetName || '').trim() || undefined,
    assetStatus: String(query.assetStatus || '').trim() || undefined
  }
}

/** 把后端原始单据状态映射到前端已有的审批流字典。 */
export function mapRealEstateStatusToWorkflow(status?: string) {
  if (status === 'pending') return 'IN_PROGRESS'
  if (status === 'approved' || status === 'completed') return 'COMPLETED'
  if (status === 'rejected') return 'REJECTED'
  return ''
}

/** 只在显式带上 `openCreate=1` 时自动打开新建弹窗。 */
export function shouldOpenCreateDialog(query: Record<string, unknown>) {
  return String(query.openCreate || '').trim() === '1'
}

/**
 * 固定资产业务页共享的最小资产上下文。
 */
interface FixedAssetBusinessContext {
  assetId?: number
  assetNo?: string
  assetName?: string
  assetStatus?: string
}

/**
 * 统一构造固定资产业务建单载荷，确保前端始终显式携带资产主键。
 */
export function buildFixedAssetBusinessPayload(
  asset: FixedAssetBusinessContext,
  reason: string
) {
  const assetId = Number(asset.assetId)
  if (!Number.isInteger(assetId) || assetId <= 0) {
    throw new Error('资产主键缺失，请从资产台账发起业务操作')
  }

  const assetNo = String(asset.assetNo || '').trim()
  if (!assetNo) {
    throw new Error('资产编号缺失，请重新选择资产')
  }

  return {
    assetId,
    assetNo,
    reason: reason.trim()
  }
}

/**
 * 领用申请仍复用共享建单载荷，保持固定资产业务口径一致。
 */
export function buildApplyRequisitionReq(
  asset: { assetId: number; assetNo: string; assetName?: string; assetStatus?: string },
  reason: string
) {
  return buildFixedAssetBusinessPayload(asset, reason)
}

/**
 * 将单据状态回退映射为后端统一的工作流状态值。
 */
export function mapRequisitionStatusToWorkflow(status: number) {
  if (status === 0) return 'pending'
  if (status === 1) return 'approved'
  if (status === 2) return 'rejected'
  if (status === 3) return 'completed'
  return ''
}

/**
 * 优先使用后端显式返回的 `wfStatus`，缺失时再按单据状态兜底。
 */
export function resolveFixedAssetWorkflowStatus(row: { status?: number; wfStatus?: string }) {
  return row.wfStatus || (typeof row.status === 'number' ? mapRequisitionStatusToWorkflow(row.status) : '')
}

/**
 * 固定资产业务单据状态文案。
 */
export function formatFixedAssetBusinessStatus(status?: number) {
  if (status === 0) return '待审批'
  if (status === 1) return '已审批'
  if (status === 2) return '已驳回'
  if (status === 3) return '已完成'
  return '--'
}

/**
 * 只有审批通过且未完成归还的记录才允许执行归还。
 */
export function canReturnAsset(row: { status: number; wfStatus?: string }) {
  return row.status === 1 && resolveFixedAssetWorkflowStatus(row) === 'approved'
}

/** 领用申请请求体，统一由资产上下文映射而来。 */
export function buildApplyRequisitionReq(
  asset: { assetId: number; assetNo: string; assetName?: string; assetStatus?: string },
  reason: string
) {
  return {
    assetId: asset.assetId,
    assetNo: asset.assetNo,
    reason: reason.trim()
  }
}

/** 把领用单状态映射为工作流字典值，避免页面重复写条件分支。 */
export function mapRequisitionStatusToWorkflow(status: number) {
  if (status === 0) return 'IN_PROGRESS'
  if (status === 1 || status === 3) return 'COMPLETED'
  if (status === 2) return 'REJECTED'
  return ''
}

/** 只有审批通过的记录才允许出现归还入口。 */
export function canReturnAsset(row: { status: number }) {
  return row.status === 1
}

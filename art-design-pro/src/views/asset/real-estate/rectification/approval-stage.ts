import type { AssetRectificationRecord } from '@/api/asset/ledger'

export type RectificationApprovalStage =
  | 'PENDING_RECTIFICATION'
  | 'PENDING_SUBMIT'
  | 'IN_REVIEW'
  | 'REJECTED_RESUBMIT'
  | 'APPROVED_CLOSED'

export type RectificationApprovalStageMeta = {
  key: RectificationApprovalStage
  label: string
  tagType: 'info' | 'warning' | 'success' | 'danger'
  nextStep: string
}

const APPROVAL_STAGE_META: Record<RectificationApprovalStage, RectificationApprovalStageMeta> = {
  PENDING_RECTIFICATION: {
    key: 'PENDING_RECTIFICATION',
    label: '整改处理中',
    tagType: 'warning',
    nextStep: '先完成整改收口，再进入审批阶段。'
  },
  PENDING_SUBMIT: {
    key: 'PENDING_SUBMIT',
    label: '待提交审批',
    tagType: 'info',
    nextStep: '整改事实已经收口，下一步提交审批并补充审批说明。'
  },
  IN_REVIEW: {
    key: 'IN_REVIEW',
    label: '审批中',
    tagType: 'warning',
    nextStep: '当前等待审批结论，可结合审批轨迹持续跟进。'
  },
  REJECTED_RESUBMIT: {
    key: 'REJECTED_RESUBMIT',
    label: '审批驳回待重提',
    tagType: 'danger',
    nextStep: '根据最新审批意见补充处理后重新提交审批。'
  },
  APPROVED_CLOSED: {
    key: 'APPROVED_CLOSED',
    label: '审批通过，已完成闭环',
    tagType: 'success',
    nextStep: '当前记录已经完成审批闭环，可归档回看。'
  }
}

export function resolveRectificationApprovalStage(record: Partial<AssetRectificationRecord>): RectificationApprovalStage {
  const rectificationStatus = String(record.rectificationStatus || '').toUpperCase()
  if (rectificationStatus !== 'COMPLETED') {
    return 'PENDING_RECTIFICATION'
  }

  const approvalStatus = String(record.approvalStatus || 'UNSUBMITTED').toUpperCase()
  if (approvalStatus === 'SUBMITTED') {
    return 'IN_REVIEW'
  }
  if (approvalStatus === 'REJECTED') {
    return 'REJECTED_RESUBMIT'
  }
  if (approvalStatus === 'APPROVED') {
    return 'APPROVED_CLOSED'
  }
  return 'PENDING_SUBMIT'
}

export function getRectificationApprovalStageMeta(
  record: Partial<AssetRectificationRecord>
): RectificationApprovalStageMeta {
  return APPROVAL_STAGE_META[resolveRectificationApprovalStage(record)]
}

export function getApprovalStatusLabel(status?: string) {
  const mapper: Record<string, string> = {
    UNSUBMITTED: '待提交审批',
    SUBMITTED: '审批中',
    APPROVED: '审批通过',
    REJECTED: '审批驳回'
  }
  return mapper[String(status || '').toUpperCase()] || '待提交审批'
}


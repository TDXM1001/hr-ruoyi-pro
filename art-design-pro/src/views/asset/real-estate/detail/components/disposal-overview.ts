import type { AssetDisposalRecord } from '@/api/asset/ledger'

export type DisposalOverviewStage =
  | 'NOT_STARTED'
  | 'PENDING_SUBMIT'
  | 'IN_REVIEW'
  | 'REJECTED_RESUBMIT'
  | 'APPROVED_DISPOSING'
  | 'DISPOSED_CLOSED'

export type DisposalOverviewSummary = {
  overallStage: DisposalOverviewStage
  overallLabel: string
  overallTagType: 'info' | 'warning' | 'success' | 'danger'
  latestActionLabel: string
  latestActionTime: string
  latestActionDesc: string
  nextStep: string
  showInitiateAction: boolean
  initiateActionLabel: string
}

const DISPOSAL_STAGE_META: Record<
  DisposalOverviewStage,
  Pick<
    DisposalOverviewSummary,
    'overallLabel' | 'overallTagType' | 'nextStep' | 'showInitiateAction' | 'initiateActionLabel'
  >
> = {
  NOT_STARTED: {
    overallLabel: '未发起处置',
    overallTagType: 'info',
    nextStep: '如确认资产已进入处置口径，请从此处发起处置并进入统一处置模块办理。',
    showInitiateAction: true,
    initiateActionLabel: '发起处置'
  },
  PENDING_SUBMIT: {
    overallLabel: '待提交处置申请',
    overallTagType: 'warning',
    nextStep: '请尽快进入统一处置模块补齐处置申请并推进正式流程。',
    showInitiateAction: true,
    initiateActionLabel: '发起处置'
  },
  IN_REVIEW: {
    overallLabel: '处置审批中',
    overallTagType: 'warning',
    nextStep: '处置审批已进入审核阶段，请持续跟进审批进度。',
    showInitiateAction: false,
    initiateActionLabel: ''
  },
  REJECTED_RESUBMIT: {
    overallLabel: '处置驳回待重提',
    overallTagType: 'danger',
    nextStep: '请根据最新处置意见补充材料后重新发起处置。',
    showInitiateAction: true,
    initiateActionLabel: '重新发起处置'
  },
  APPROVED_DISPOSING: {
    overallLabel: '审批通过待完成处置',
    overallTagType: 'warning',
    nextStep: '审批已通过，请进入统一处置模块完成最终处置确认。',
    showInitiateAction: false,
    initiateActionLabel: ''
  },
  DISPOSED_CLOSED: {
    overallLabel: '已完成处置闭环',
    overallTagType: 'success',
    nextStep: '处置已闭环，可回看历史记录并归档留痕。',
    showInitiateAction: false,
    initiateActionLabel: ''
  }
}

export function buildDisposalOverviewSummary(
  disposalRecords: AssetDisposalRecord[],
  assetStatus?: string
): DisposalOverviewSummary {
  const overallStage = resolveDisposalOverviewStage(disposalRecords, assetStatus)
  const meta = DISPOSAL_STAGE_META[overallStage]
  const latestAction = resolveLatestDisposalAction(disposalRecords, assetStatus, overallStage)

  return {
    overallStage,
    overallLabel: meta.overallLabel,
    overallTagType: meta.overallTagType,
    latestActionLabel: latestAction.label,
    latestActionTime: latestAction.time,
    latestActionDesc: latestAction.desc,
    nextStep: meta.nextStep,
    showInitiateAction: meta.showInitiateAction,
    initiateActionLabel: meta.initiateActionLabel
  }
}

function resolveDisposalOverviewStage(
  disposalRecords: AssetDisposalRecord[],
  assetStatus?: string
): DisposalOverviewStage {
  const latestRecord = getLatestDisposalRecord(disposalRecords)
  const recordStatus = String(latestRecord?.disposalStatus || '').toUpperCase()

  if (recordStatus === 'CONFIRMED') {
    return 'DISPOSED_CLOSED'
  }
  if (recordStatus === 'APPROVED') {
    return 'APPROVED_DISPOSING'
  }
  if (recordStatus === 'SUBMITTED') {
    return 'IN_REVIEW'
  }
  if (recordStatus === 'REJECTED' || recordStatus === 'CANCELLED') {
    return 'REJECTED_RESUBMIT'
  }
  if (recordStatus === 'PENDING') {
    return 'PENDING_SUBMIT'
  }

  const normalizedAssetStatus = String(assetStatus || '').toUpperCase()
  if (normalizedAssetStatus === 'PENDING_DISPOSAL') {
    return 'PENDING_SUBMIT'
  }
  if (normalizedAssetStatus === 'DISPOSED') {
    return 'DISPOSED_CLOSED'
  }

  return 'NOT_STARTED'
}

function resolveLatestDisposalAction(
  disposalRecords: AssetDisposalRecord[],
  assetStatus: string | undefined,
  overallStage: DisposalOverviewStage
) {
  const latestRecord = getLatestDisposalRecord(disposalRecords)
  if (!latestRecord) {
    if (String(assetStatus || '').toUpperCase() === 'PENDING_DISPOSAL') {
      return {
        label: '进入待处置口径',
        time: '-',
        desc: '资产状态已进入待处置，但还需要在统一处置模块补齐正式申请。'
      }
    }

    return {
      label: '未发起处置',
      time: '-',
      desc: '当前资产还没有进入处置流程。'
    }
  }

  const status = String(latestRecord.disposalStatus || '').toUpperCase()
  const disposalNo = latestRecord.disposalNo || '处置记录'
  const actionTime = latestRecord.confirmedTime || latestRecord.disposalDate || '-'

  if (status === 'CONFIRMED') {
    return {
      label: '已确认处置',
      time: actionTime,
      desc: `${disposalNo} 已完成处置确认。`
    }
  }
  if (status === 'APPROVED') {
    return {
      label: '处置审批通过',
      time: actionTime,
      desc: `${disposalNo} 审批已通过，等待完成最终处置确认。`
    }
  }
  if (status === 'SUBMITTED') {
    return {
      label: '提交处置审批',
      time: actionTime,
      desc: `${disposalNo} 已提交审批，等待审核结果。`
    }
  }
  if (status === 'REJECTED' || status === 'CANCELLED') {
    return {
      label: '处置审批驳回',
      time: actionTime,
      desc: latestRecord.disposalReason || `${disposalNo} 已被驳回或取消，请重新整理处置信息。`
    }
  }
  if (status === 'PENDING') {
    return {
      label: '待提交处置申请',
      time: actionTime,
      desc: latestRecord.disposalReason || `${disposalNo} 已创建，待进入正式处置流程。`
    }
  }

  return {
    label: DISPOSAL_STAGE_META[overallStage].overallLabel,
    time: actionTime,
    desc: latestRecord.disposalReason || `${disposalNo} 仍在处置流程中。`
  }
}

function getLatestDisposalRecord(disposalRecords: AssetDisposalRecord[]) {
  return [...disposalRecords].sort((left, right) => {
    return toTimestamp(resolveRecordLatestTime(right)) - toTimestamp(resolveRecordLatestTime(left))
  })[0]
}

function resolveRecordLatestTime(record: AssetDisposalRecord) {
  return record.confirmedTime || record.disposalDate || ''
}

function toTimestamp(value?: string) {
  if (!value) {
    return 0
  }
  const parsed = new Date(value.replace(/-/g, '/')).getTime()
  return Number.isNaN(parsed) ? 0 : parsed
}

import type { AssetChangeLogRecord, AssetDisposalRecord } from '@/api/asset/ledger'
import type { OverviewLifecycleRecord } from './rectification-overview'

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
  latestActionOwner: string
  latestActionDesc: string
  nextStep: string
  responsibilityOwnerLabel: string
  responsibilityActionLabel: string
  responsibilityHint: string
  showInitiateAction: boolean
  initiateActionLabel: string
}

export type DisposalResponsibilityView = {
  ownerLabel: string
  actionLabel: string
  hint: string
  latestOwner: string
}

type DisposalEventMeta = Pick<
  OverviewLifecycleRecord,
  'disposalEventKey' | 'disposalEventLabel' | 'disposalEventTagType' | 'disposalEventHint'
>

const DISPOSAL_STAGE_META: Record<
  DisposalOverviewStage,
  Pick<
    DisposalOverviewSummary,
    | 'overallLabel'
    | 'overallTagType'
    | 'nextStep'
    | 'responsibilityOwnerLabel'
    | 'responsibilityActionLabel'
    | 'responsibilityHint'
    | 'showInitiateAction'
    | 'initiateActionLabel'
  >
> = {
  NOT_STARTED: {
    overallLabel: '未发起处置',
    overallTagType: 'info',
    nextStep: '如确认资产已进入处置口径，请从此处发起处置并进入统一处置模块办理。',
    responsibilityOwnerLabel: '资产管理员',
    responsibilityActionLabel: '发起处置',
    responsibilityHint: '请先由资产管理员确认处置依据，再从不动产详情页发起处置流程。',
    showInitiateAction: true,
    initiateActionLabel: '发起处置'
  },
  PENDING_SUBMIT: {
    overallLabel: '待提交处置申请',
    overallTagType: 'warning',
    nextStep: '请尽快进入统一处置模块补齐处置申请并推进正式流程。',
    responsibilityOwnerLabel: '资产管理员',
    responsibilityActionLabel: '补齐申请',
    responsibilityHint: '由资产管理员补齐处置申请、原因和材料后提交统一处置流程。',
    showInitiateAction: true,
    initiateActionLabel: '发起处置'
  },
  IN_REVIEW: {
    overallLabel: '处置审批中',
    overallTagType: 'warning',
    nextStep: '处置审批已进入审核阶段，请持续跟进审批进度。',
    responsibilityOwnerLabel: '审批责任岗',
    responsibilityActionLabel: '审批处理',
    responsibilityHint: '当前由审批责任岗处理，资产管理员需跟进审批反馈并准备补充材料。',
    showInitiateAction: false,
    initiateActionLabel: ''
  },
  REJECTED_RESUBMIT: {
    overallLabel: '处置驳回待重提',
    overallTagType: 'danger',
    nextStep: '请根据最新处置意见补充材料后重新发起处置。',
    responsibilityOwnerLabel: '资产管理员',
    responsibilityActionLabel: '重提申请',
    responsibilityHint: '由资产管理员根据驳回意见补齐材料，并重新提交处置申请。',
    showInitiateAction: true,
    initiateActionLabel: '重新发起处置'
  },
  APPROVED_DISPOSING: {
    overallLabel: '审批通过待完成处置',
    overallTagType: 'warning',
    nextStep: '审批已通过，请进入统一处置模块完成最终处置确认。',
    responsibilityOwnerLabel: '资产管理员 / 执行责任岗',
    responsibilityActionLabel: '完成确认',
    responsibilityHint: '审批已通过，由资产管理员协调执行责任岗完成最终处置确认。',
    showInitiateAction: false,
    initiateActionLabel: ''
  },
  DISPOSED_CLOSED: {
    overallLabel: '已完成处置闭环',
    overallTagType: 'success',
    nextStep: '处置已闭环，可回看历史记录并归档留痕。',
    responsibilityOwnerLabel: '资产管理员',
    responsibilityActionLabel: '归档回看',
    responsibilityHint: '处置已闭环，由资产管理员回看结果并完成留痕归档。',
    showInitiateAction: false,
    initiateActionLabel: ''
  }
}

const DISPOSAL_EVENT_MATCHERS: Array<{
  matcher: (changeDesc: string) => boolean
  meta: DisposalEventMeta
}> = [
  {
    matcher: (changeDesc) => changeDesc.includes('确认处置'),
    meta: {
      disposalEventKey: 'DISPOSED_CLOSED',
      disposalEventLabel: '确认处置',
      disposalEventTagType: 'success',
      disposalEventHint: '处置已完成确认，可回看历史并归档留痕。'
    }
  },
  {
    matcher: (changeDesc) => changeDesc.includes('处置审批通过'),
    meta: {
      disposalEventKey: 'APPROVED_DISPOSING',
      disposalEventLabel: '审批通过',
      disposalEventTagType: 'success',
      disposalEventHint: '处置审批已通过，下一步需要完成最终处置确认。'
    }
  },
  {
    matcher: (changeDesc) => changeDesc.includes('提交处置审批'),
    meta: {
      disposalEventKey: 'IN_REVIEW',
      disposalEventLabel: '提交审批',
      disposalEventTagType: 'warning',
      disposalEventHint: '处置申请已提交，当前正在等待审批结论。'
    }
  },
  {
    matcher: (changeDesc) => changeDesc.includes('处置审批驳回'),
    meta: {
      disposalEventKey: 'REJECTED_RESUBMIT',
      disposalEventLabel: '审批驳回',
      disposalEventTagType: 'danger',
      disposalEventHint: '处置审批已驳回，需要根据意见补齐材料并重新提交流程。'
    }
  },
  {
    matcher: (changeDesc) => changeDesc.includes('发起处置') || changeDesc.includes('创建处置单'),
    meta: {
      disposalEventKey: 'PENDING_SUBMIT',
      disposalEventLabel: '发起处置',
      disposalEventTagType: 'info',
      disposalEventHint: '处置已进入办理链路，下一步需要补齐申请并推进审批。'
    }
  }
]

export function buildDisposalOverviewSummary(
  disposalRecords: AssetDisposalRecord[],
  assetStatus?: string,
  changeLogs: Array<AssetChangeLogRecord | OverviewLifecycleRecord> = []
): DisposalOverviewSummary {
  const overallStage = resolveDisposalOverviewStage(disposalRecords, assetStatus)
  const meta = DISPOSAL_STAGE_META[overallStage]
  const latestAction = resolveLatestDisposalAction(disposalRecords, assetStatus, overallStage, changeLogs)
  const responsibilityView = buildDisposalResponsibilityView(overallStage, latestAction.owner)

  return {
    overallStage,
    overallLabel: meta.overallLabel,
    overallTagType: meta.overallTagType,
    latestActionLabel: latestAction.label,
    latestActionTime: latestAction.time,
    latestActionOwner: latestAction.owner,
    latestActionDesc: latestAction.desc,
    nextStep: meta.nextStep,
    responsibilityOwnerLabel: responsibilityView.ownerLabel,
    responsibilityActionLabel: responsibilityView.actionLabel,
    responsibilityHint: responsibilityView.hint,
    showInitiateAction: meta.showInitiateAction,
    initiateActionLabel: meta.initiateActionLabel
  }
}

export function buildDisposalResponsibilityView(
  stage: DisposalOverviewStage,
  latestOwner?: string
): DisposalResponsibilityView {
  const meta = DISPOSAL_STAGE_META[stage]
  return {
    ownerLabel: meta.responsibilityOwnerLabel,
    actionLabel: meta.responsibilityActionLabel,
    hint: meta.responsibilityHint,
    latestOwner: latestOwner || '-'
  }
}

export function decorateDisposalLifecycleEvent(changeDesc?: string): DisposalEventMeta {
  const normalizedDesc = String(changeDesc || '')
  const matched = DISPOSAL_EVENT_MATCHERS.find((item) => item.matcher(normalizedDesc))
  return matched?.meta || {}
}

export function resolveDisposalOverviewStage(
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
  overallStage: DisposalOverviewStage,
  changeLogs: Array<AssetChangeLogRecord | OverviewLifecycleRecord>
) {
  const latestLog = getLatestDisposalLog(changeLogs)
  if (latestLog?.disposalEventLabel) {
    return {
      label: latestLog.disposalEventLabel,
      time: latestLog.operateTime || '-',
      owner: latestLog.operateBy || '-',
      desc: latestLog.changeDesc || latestLog.disposalEventHint || '暂无动作说明'
    }
  }

  const latestRecord = getLatestDisposalRecord(disposalRecords)
  if (!latestRecord) {
    if (String(assetStatus || '').toUpperCase() === 'PENDING_DISPOSAL') {
      return {
        label: '进入待处置口径',
        time: '-',
        owner: '资产管理员',
        desc: '资产状态已进入待处置，但还需要在统一处置模块补齐正式申请。'
      }
    }

    return {
      label: '未发起处置',
      time: '-',
      owner: '-',
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
      owner: latestRecord.confirmedBy || '-',
      desc: `${disposalNo} 已完成处置确认。`
    }
  }
  if (status === 'APPROVED') {
    return {
      label: '处置审批通过',
      time: actionTime,
      owner: latestRecord.confirmedBy || '-',
      desc: `${disposalNo} 审批已通过，等待完成最终处置确认。`
    }
  }
  if (status === 'SUBMITTED') {
    return {
      label: '提交处置审批',
      time: actionTime,
      owner: latestRecord.confirmedBy || '-',
      desc: `${disposalNo} 已提交审批，等待审核结果。`
    }
  }
  if (status === 'REJECTED' || status === 'CANCELLED') {
    return {
      label: '处置审批驳回',
      time: actionTime,
      owner: latestRecord.confirmedBy || '-',
      desc: latestRecord.disposalReason || `${disposalNo} 已被驳回或取消，请重新整理处置信息。`
    }
  }
  if (status === 'PENDING') {
    return {
      label: '待提交处置申请',
      time: actionTime,
      owner: latestRecord.confirmedBy || '-',
      desc: latestRecord.disposalReason || `${disposalNo} 已创建，待进入正式处置流程。`
    }
  }

  return {
    label: DISPOSAL_STAGE_META[overallStage].overallLabel,
    time: actionTime,
    owner: latestRecord.confirmedBy || '-',
    desc: latestRecord.disposalReason || `${disposalNo} 仍在处置流程中。`
  }
}

function getLatestDisposalLog(changeLogs: Array<AssetChangeLogRecord | OverviewLifecycleRecord>) {
  return [...changeLogs]
    .map((record) => ({
      ...record,
      ...decorateDisposalLifecycleEvent(record.changeDesc)
    }))
    .filter((record) => !!record.disposalEventKey)
    .sort((left, right) => toTimestamp(right.operateTime) - toTimestamp(left.operateTime))[0]
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

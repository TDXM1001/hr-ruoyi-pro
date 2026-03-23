import type { AssetChangeLogRecord, AssetRectificationRecord } from '@/api/asset/ledger'

export type RectificationOverviewStage =
  | 'PENDING_RECTIFICATION'
  | 'PENDING_SUBMIT'
  | 'IN_REVIEW'
  | 'REJECTED_RESUBMIT'
  | 'APPROVED_CLOSED'
  | 'NO_RECTIFICATION'

export type RectificationOverviewSummary = {
  pendingRectificationCount: number
  pendingSubmitCount: number
  inReviewCount: number
  rejectedResubmitCount: number
  approvedClosedCount: number
  overallStage: RectificationOverviewStage
  overallLabel: string
  overallTagType: 'info' | 'warning' | 'success' | 'danger'
  latestActionLabel: string
  latestActionTime: string
  latestActionDesc: string
  nextStep: string
}

export type OverviewLifecycleRecord = AssetChangeLogRecord & {
  rectificationEventKey?: string
  rectificationEventLabel?: string
  rectificationEventTagType?: 'info' | 'warning' | 'success' | 'danger'
  rectificationEventHint?: string
  disposalEventKey?: string
  disposalEventLabel?: string
  disposalEventTagType?: 'info' | 'warning' | 'success' | 'danger'
  disposalEventHint?: string
}

type RectificationEventMeta = Pick<
  OverviewLifecycleRecord,
  'rectificationEventKey' | 'rectificationEventLabel' | 'rectificationEventTagType' | 'rectificationEventHint'
>

const NO_RECTIFICATION_SUMMARY: RectificationOverviewSummary = {
  pendingRectificationCount: 0,
  pendingSubmitCount: 0,
  inReviewCount: 0,
  rejectedResubmitCount: 0,
  approvedClosedCount: 0,
  overallStage: 'NO_RECTIFICATION',
  overallLabel: '暂无整改记录',
  overallTagType: 'info',
  latestActionLabel: '暂无整改动作',
  latestActionTime: '-',
  latestActionDesc: '当前资产还没有进入整改闭环链路。',
  nextStep: '当前暂无整改任务，继续关注巡检和处置联动即可。'
}

const RECTIFICATION_EVENT_MATCHERS: Array<{
  keyword: string
  meta: RectificationEventMeta
}> = [
  {
    keyword: '审批驳回',
    meta: {
      rectificationEventKey: 'REJECTED_RESUBMIT',
      rectificationEventLabel: '审批驳回',
      rectificationEventTagType: 'danger',
      rectificationEventHint: '审批已驳回，需要根据意见补齐材料后重新提交。'
    }
  },
  {
    keyword: '审批通过',
    meta: {
      rectificationEventKey: 'APPROVED_CLOSED',
      rectificationEventLabel: '审批通过',
      rectificationEventTagType: 'success',
      rectificationEventHint: '整改审批已通过，可归档回看。'
    }
  },
  {
    keyword: '提交整改审批',
    meta: {
      rectificationEventKey: 'IN_REVIEW',
      rectificationEventLabel: '提交审批',
      rectificationEventTagType: 'info',
      rectificationEventHint: '整改已完成，下一步需要关注审批是否推进。'
    }
  },
  {
    keyword: '完成整改单',
    meta: {
      rectificationEventKey: 'PENDING_SUBMIT',
      rectificationEventLabel: '完成整改',
      rectificationEventTagType: 'success',
      rectificationEventHint: '整改事实已收口，下一步建议尽快提交审批。'
    }
  },
  {
    keyword: '发起整改单',
    meta: {
      rectificationEventKey: 'PENDING_RECTIFICATION',
      rectificationEventLabel: '发起整改',
      rectificationEventTagType: 'warning',
      rectificationEventHint: '整改已进入处理链路，当前仍需推进整改事实收口。'
    }
  }
]

export function decorateOverviewLifecycleRecords(
  changeLogs: AssetChangeLogRecord[]
): OverviewLifecycleRecord[] {
  return changeLogs.map((record) => ({
    ...record,
    ...resolveRectificationEvent(record.changeDesc)
  }))
}

export function buildRectificationOverviewSummary(
  rectificationRecords: AssetRectificationRecord[],
  changeLogs: OverviewLifecycleRecord[]
): RectificationOverviewSummary {
  if (!rectificationRecords.length && !changeLogs.some((record) => record.rectificationEventKey)) {
    return NO_RECTIFICATION_SUMMARY
  }

  const stageCounters = {
    pendingRectificationCount: 0,
    pendingSubmitCount: 0,
    inReviewCount: 0,
    rejectedResubmitCount: 0,
    approvedClosedCount: 0
  }

  rectificationRecords.forEach((record) => {
    const stage = resolveRectificationOverviewStage(record)
    if (stage === 'PENDING_RECTIFICATION') {
      stageCounters.pendingRectificationCount += 1
    } else if (stage === 'PENDING_SUBMIT') {
      stageCounters.pendingSubmitCount += 1
    } else if (stage === 'IN_REVIEW') {
      stageCounters.inReviewCount += 1
    } else if (stage === 'REJECTED_RESUBMIT') {
      stageCounters.rejectedResubmitCount += 1
    } else if (stage === 'APPROVED_CLOSED') {
      stageCounters.approvedClosedCount += 1
    }
  })

  const overallStage = resolveOverallStage(stageCounters)
  const latestAction = resolveLatestRectificationAction(changeLogs, rectificationRecords)

  return {
    ...stageCounters,
    overallStage,
    overallLabel: getOverviewStageLabel(overallStage),
    overallTagType: getOverviewStageTagType(overallStage),
    latestActionLabel: latestAction.label,
    latestActionTime: latestAction.time,
    latestActionDesc: latestAction.desc,
    nextStep: resolveNextStep(stageCounters)
  }
}

function resolveRectificationOverviewStage(
  record: Partial<AssetRectificationRecord>
): RectificationOverviewStage {
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

function resolveOverallStage(counters: {
  pendingRectificationCount: number
  pendingSubmitCount: number
  inReviewCount: number
  rejectedResubmitCount: number
  approvedClosedCount: number
}): RectificationOverviewStage {
  if (counters.rejectedResubmitCount > 0) {
    return 'REJECTED_RESUBMIT'
  }
  if (counters.inReviewCount > 0) {
    return 'IN_REVIEW'
  }
  if (counters.pendingSubmitCount > 0) {
    return 'PENDING_SUBMIT'
  }
  if (counters.pendingRectificationCount > 0) {
    return 'PENDING_RECTIFICATION'
  }
  if (counters.approvedClosedCount > 0) {
    return 'APPROVED_CLOSED'
  }
  return 'NO_RECTIFICATION'
}

function getOverviewStageLabel(stage: RectificationOverviewStage) {
  const mapper: Record<RectificationOverviewStage, string> = {
    PENDING_RECTIFICATION: '待整改',
    PENDING_SUBMIT: '待提交审批',
    IN_REVIEW: '审批中',
    REJECTED_RESUBMIT: '审批驳回待重提',
    APPROVED_CLOSED: '审批通过，已完成闭环',
    NO_RECTIFICATION: '暂无整改记录'
  }
  return mapper[stage]
}

function getOverviewStageTagType(stage: RectificationOverviewStage): 'info' | 'warning' | 'success' | 'danger' {
  const mapper: Record<RectificationOverviewStage, 'info' | 'warning' | 'success' | 'danger'> = {
    PENDING_RECTIFICATION: 'warning',
    PENDING_SUBMIT: 'info',
    IN_REVIEW: 'warning',
    REJECTED_RESUBMIT: 'danger',
    APPROVED_CLOSED: 'success',
    NO_RECTIFICATION: 'info'
  }
  return mapper[stage]
}

function resolveNextStep(counters: {
  pendingRectificationCount: number
  pendingSubmitCount: number
  inReviewCount: number
  rejectedResubmitCount: number
  approvedClosedCount: number
}) {
  if (counters.rejectedResubmitCount > 0) {
    return '优先根据最新审批意见补充材料，并重新提交审批。'
  }
  if (counters.inReviewCount > 0) {
    return '当前已有整改记录进入审批阶段，建议继续关注审批轨迹和结论。'
  }
  if (counters.pendingSubmitCount > 0) {
    return '整改事实已经收口，但仍需尽快提交审批，避免闭环停在待提交阶段。'
  }
  if (counters.pendingRectificationCount > 0) {
    return '当前仍有待整改记录，建议优先补齐责任、期限并推进整改完成。'
  }
  if (counters.approvedClosedCount > 0) {
    return '当前整改记录已完成审批闭环，可回看生命周期留痕并继续关注其他主线。'
  }
  return NO_RECTIFICATION_SUMMARY.nextStep
}

function resolveLatestRectificationAction(
  changeLogs: OverviewLifecycleRecord[],
  rectificationRecords: AssetRectificationRecord[]
) {
  const latestLog = [...changeLogs]
    .filter((record) => !!record.rectificationEventKey)
    .sort((left, right) => toTimestamp(right.operateTime) - toTimestamp(left.operateTime))[0]

  if (latestLog) {
    return {
      label: latestLog.rectificationEventLabel || '整改动作',
      time: latestLog.operateTime || '-',
      desc: latestLog.changeDesc || latestLog.rectificationEventHint || '暂无动作说明'
    }
  }

  const latestRecord = [...rectificationRecords].sort((left, right) => {
    return toTimestamp(resolveRecordLatestTime(right)) - toTimestamp(resolveRecordLatestTime(left))
  })[0]

  if (!latestRecord) {
    return {
      label: NO_RECTIFICATION_SUMMARY.latestActionLabel,
      time: NO_RECTIFICATION_SUMMARY.latestActionTime,
      desc: NO_RECTIFICATION_SUMMARY.latestActionDesc
    }
  }

  return {
    label: getOverviewStageLabel(resolveRectificationOverviewStage(latestRecord)),
    time: resolveRecordLatestTime(latestRecord) || '-',
    desc: latestRecord.latestApprovalOpinion || latestRecord.completionDesc || latestRecord.issueDesc || '暂无动作说明'
  }
}

function resolveRecordLatestTime(record: Partial<AssetRectificationRecord>) {
  return (
    record.latestApprovalOperateTime ||
    record.approvalFinishedTime ||
    record.approvalSubmittedTime ||
    record.completedTime ||
    record.createTime ||
    ''
  )
}

function resolveRectificationEvent(changeDesc?: string): RectificationEventMeta {
  const normalizedDesc = String(changeDesc || '')
  if (normalizedDesc.includes('处置')) {
    return {}
  }
  const matcher = RECTIFICATION_EVENT_MATCHERS.find((item) => normalizedDesc.includes(item.keyword))
  return matcher?.meta || {}
}

function toTimestamp(value?: string) {
  if (!value) {
    return 0
  }
  const parsed = new Date(value.replace(/-/g, '/')).getTime()
  return Number.isNaN(parsed) ? 0 : parsed
}

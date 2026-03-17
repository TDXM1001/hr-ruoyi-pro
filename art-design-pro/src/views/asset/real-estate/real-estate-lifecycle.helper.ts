import type { CreateRealEstateDisposalReq } from '@/api/asset/real-estate-disposal'
import type { CreateOwnershipChangeReq } from '@/api/asset/real-estate-ownership'
import type { CreateStatusChangeReq } from '@/api/asset/real-estate-status'
import type { CreateUsageChangeReq } from '@/api/asset/real-estate-usage'
import {
  formatAssetTimelineAction,
  formatAssetTimelineDocStatus,
  type AssetBusinessOrderBase,
  type AssetRef,
  type AssetTimelineItem
} from '@/types/asset'

/** 不动产业务页路由携带的资产上下文。 */
export interface RealEstateRouteAssetContext {
  assetId?: number
  assetNo: string
  assetName?: string
  assetStatus?: string
  latestActionType?: AssetTimelineItem['actionType']
  latestActionLabel?: AssetTimelineItem['actionLabel']
  latestDocStatus?: AssetTimelineItem['docStatus']
  latestActionTime?: AssetTimelineItem['actionTime']
}

/** 不动产业务状态选项。 */
export interface RealEstateStatusOption {
  label: string
  value: string
}

/** 不动产业务动作标识。 */
export type RealEstateActionKey =
  | 'realEstateOwnership'
  | 'realEstateUsage'
  | 'realEstateStatus'
  | 'realEstateDisposal'

/** 不动产业务动作可用性结果。 */
export interface RealEstateActionGuard {
  disabled: boolean
  reason?: string
  latestActionText?: string
}

/** 不动产业务统一要求从资产台账带入资产主键。 */
export const REAL_ESTATE_ENTRY_ERROR = '请从资产台账选择不动产后再发起业务单据'

/** 不动产业务统一要求必须保留资产编号。 */
export const REAL_ESTATE_ASSET_NO_ERROR = '缺少不动产资产编号，请从资产台账重新发起业务单据'

/** 审批类不动产业务的单据状态选项。 */
export const REAL_ESTATE_APPROVAL_STATUS_OPTIONS: RealEstateStatusOption[] = [
  { label: '待审批', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' }
]

/** 直达类不动产业务的单据状态选项。 */
export const REAL_ESTATE_DIRECT_STATUS_OPTIONS: RealEstateStatusOption[] = [
  { label: '已完成', value: 'completed' }
]

type RealEstateOrderAssetRef = Pick<AssetRef, 'assetId' | 'assetNo'>
type RealEstateWorkflowRow = Partial<Pick<AssetBusinessOrderBase, 'status' | 'wfStatus'>>

interface OwnershipChangeDraft {
  assetNo?: string
  targetRightsHolder: string
  targetPropertyCertNo?: string
  targetRegistrationDate?: string
  reason: string
}

interface UsageChangeDraft {
  assetNo?: string
  targetLandUse?: string
  targetBuildingUse?: string
  reason: string
}

interface StatusChangeDraft {
  assetNo?: string
  targetAssetStatus: string
  reason: string
}

interface DisposalDraft {
  assetNo?: string
  disposalType?: string
  targetAssetStatus?: string
  reason: string
}

const REAL_ESTATE_TERMINAL_STATUSES = new Set(['5', '6'])

const REAL_ESTATE_ACTION_TYPE_MAP: Record<RealEstateActionKey, string> = {
  realEstateOwnership: 'REAL_ESTATE_OWNERSHIP_CHANGE',
  realEstateUsage: 'REAL_ESTATE_USAGE_CHANGE',
  realEstateStatus: 'REAL_ESTATE_STATUS_CHANGE',
  realEstateDisposal: 'REAL_ESTATE_DISPOSAL'
}

function normalizeText(value: unknown) {
  return String(value ?? '').trim()
}

function normalizeOptionalText(value: unknown) {
  return normalizeText(value) || undefined
}

function isValidAssetId(value: unknown) {
  const assetId = Number(value)
  return Number.isInteger(assetId) && assetId > 0
}

/** 从路由 query 中解析不动产业务页的资产上下文。 */
export function parseAssetRouteQuery(
  query: Record<string, unknown>
): RealEstateRouteAssetContext | null {
  const assetNo = normalizeText(query.assetNo)
  if (!assetNo) {
    return null
  }

  const assetId = Number(query.assetId)
  return {
    assetId: Number.isNaN(assetId) ? undefined : assetId,
    assetNo,
    assetName: normalizeOptionalText(query.assetName),
    assetStatus: normalizeOptionalText(query.assetStatus),
    latestActionType: normalizeOptionalText(query.latestActionType) as
      | AssetTimelineItem['actionType']
      | undefined,
    latestActionLabel: normalizeOptionalText(query.latestActionLabel),
    latestDocStatus: normalizeOptionalText(query.latestDocStatus) as
      | AssetTimelineItem['docStatus']
      | undefined,
    latestActionTime: normalizeOptionalText(query.latestActionTime)
  }
}

/** 允许仅靠 route query 做动作守卫判断，但不会放宽建单时的资产主键要求。 */
function buildLooseRouteContext(query: Record<string, unknown>): RealEstateRouteAssetContext {
  const assetId = Number(query.assetId)
  return {
    assetId: Number.isNaN(assetId) ? undefined : assetId,
    assetNo: normalizeText(query.assetNo),
    assetName: normalizeOptionalText(query.assetName),
    assetStatus: normalizeOptionalText(query.assetStatus),
    latestActionType: normalizeOptionalText(query.latestActionType) as
      | AssetTimelineItem['actionType']
      | undefined,
    latestActionLabel: normalizeOptionalText(query.latestActionLabel),
    latestDocStatus: normalizeOptionalText(query.latestDocStatus) as
      | AssetTimelineItem['docStatus']
      | undefined,
    latestActionTime: normalizeOptionalText(query.latestActionTime)
  }
}

/** 将不动产业务状态映射到流程状态字典。 */
export function mapRealEstateStatusToWorkflow(status?: string) {
  if (status === 'pending') return 'IN_PROGRESS'
  if (status === 'approved' || status === 'completed') return 'COMPLETED'
  if (status === 'rejected') return 'REJECTED'
  return ''
}

/** 统一解析流程状态，优先使用后端显式返回的 wfStatus。 */
export function resolveRealEstateWorkflowStatus(row?: RealEstateWorkflowRow | null) {
  const workflowStatus =
    normalizeOptionalText(row?.wfStatus) ||
    normalizeOptionalText(row?.status as string | number | undefined)
  return mapRealEstateStatusToWorkflow(workflowStatus)
}

/** 统一解析单据状态值，供页面直接渲染字典。 */
export function resolveRealEstateDocumentStatus(
  row?: Pick<AssetBusinessOrderBase, 'status'> | null
) {
  return normalizeOptionalText(row?.status as string | number | undefined) || ''
}

/** 格式化路由里携带的最近一条业务快照。 */
export function formatRealEstateLatestAction(context: RealEstateRouteAssetContext | null) {
  if (!context?.latestActionType) {
    return ''
  }

  const actionLabel = context.latestActionLabel || formatAssetTimelineAction(context.latestActionType)
  const statusLabel = formatAssetTimelineDocStatus(context.latestDocStatus)
  const timeText = context.latestActionTime ? ` ${context.latestActionTime}` : ''
  return `${actionLabel} / ${statusLabel}${timeText}`
}

/** 统一校验不动产业务建单必须带上资产主键和资产编号。 */
function resolveRealEstateOrderAssetRef(
  context: RealEstateRouteAssetContext | null,
  fallbackAssetNo?: string
): RealEstateOrderAssetRef {
  if (!isValidAssetId(context?.assetId)) {
    throw new Error(REAL_ESTATE_ENTRY_ERROR)
  }

  const assetNo = normalizeText(context?.assetNo || fallbackAssetNo)
  if (!assetNo) {
    throw new Error(REAL_ESTATE_ASSET_NO_ERROR)
  }

  return {
    assetId: Number(context?.assetId),
    assetNo
  }
}

/** 统一构造不产权属变更 payload。 */
export function buildOwnershipChangePayload(
  context: RealEstateRouteAssetContext | null,
  draft: OwnershipChangeDraft
): CreateOwnershipChangeReq {
  return {
    ...resolveRealEstateOrderAssetRef(context, draft.assetNo),
    targetRightsHolder: normalizeText(draft.targetRightsHolder),
    targetPropertyCertNo: normalizeOptionalText(draft.targetPropertyCertNo),
    targetRegistrationDate: normalizeOptionalText(draft.targetRegistrationDate),
    reason: normalizeText(draft.reason)
  }
}

/** 统一构造不动产用途变更 payload。 */
export function buildUsageChangePayload(
  context: RealEstateRouteAssetContext | null,
  draft: UsageChangeDraft
): CreateUsageChangeReq {
  return {
    ...resolveRealEstateOrderAssetRef(context, draft.assetNo),
    targetLandUse: normalizeOptionalText(draft.targetLandUse),
    targetBuildingUse: normalizeOptionalText(draft.targetBuildingUse),
    reason: normalizeText(draft.reason)
  }
}

/** 统一构造不动产状态变更 payload。 */
export function buildStatusChangePayload(
  context: RealEstateRouteAssetContext | null,
  draft: StatusChangeDraft
): CreateStatusChangeReq {
  return {
    ...resolveRealEstateOrderAssetRef(context, draft.assetNo),
    targetAssetStatus: normalizeText(draft.targetAssetStatus),
    reason: normalizeText(draft.reason)
  }
}

/** 统一构造不动产注销/处置 payload。 */
export function buildRealEstateDisposalPayload(
  context: RealEstateRouteAssetContext | null,
  draft: DisposalDraft
): CreateRealEstateDisposalReq {
  return {
    ...resolveRealEstateOrderAssetRef(context, draft.assetNo),
    disposalType: normalizeOptionalText(draft.disposalType),
    targetAssetStatus: normalizeOptionalText(draft.targetAssetStatus) || '6',
    reason: normalizeText(draft.reason)
  }
}

/** 统一计算不动产业务动作是否可发起。 */
export function getRealEstateActionGuard(
  actionKey: RealEstateActionKey,
  context: RealEstateRouteAssetContext | null
): RealEstateActionGuard {
  const latestActionText = formatRealEstateLatestAction(context)

  if (!context || !isValidAssetId(context.assetId) || !normalizeText(context.assetNo)) {
    return {
      disabled: true,
      reason: REAL_ESTATE_ENTRY_ERROR,
      latestActionText
    }
  }

  if (REAL_ESTATE_TERMINAL_STATUSES.has(context.assetStatus || '')) {
    return {
      disabled: true,
      reason: '当前不动产已处于终态，不能继续发起新的业务单据。',
      latestActionText
    }
  }

  if (
    context.latestActionType === 'REAL_ESTATE_DISPOSAL' &&
    context.latestDocStatus === 'pending' &&
    actionKey !== 'realEstateDisposal'
  ) {
    return {
      disabled: true,
      reason: '当前不动产存在待审批的注销/处置单，需先处理完成后再发起其他业务。',
      latestActionText
    }
  }

  if (
    context.latestActionType === REAL_ESTATE_ACTION_TYPE_MAP[actionKey] &&
    context.latestDocStatus === 'pending'
  ) {
    return {
      disabled: true,
      reason: '当前不动产存在同类型待审批单据，需先处理完成后再重复发起。',
      latestActionText
    }
  }

  return { disabled: false, latestActionText }
}

/** 仅当 query 明确要求且动作守卫允许时，才自动打开建单弹窗。 */
export function shouldOpenCreateDialog(query: Record<string, unknown>) {
  if (normalizeText(query.openCreate) !== '1') {
    return false
  }

  const actionKey = normalizeText(query.actionKey) as RealEstateActionKey
  if (!actionKey) {
    return false
  }

  const context = parseAssetRouteQuery(query) || buildLooseRouteContext(query)
  return !getRealEstateActionGuard(actionKey, context).disabled
}

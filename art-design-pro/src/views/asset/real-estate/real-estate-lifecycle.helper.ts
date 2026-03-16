import {
  formatAssetTimelineAction,
  formatAssetTimelineDocStatus,
  type AssetTimelineItem
} from '@/types/asset'

/** 不动产动作页从资产台账透传的最小上下文。 */
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

/** 搜索下拉使用的最小选项结构。 */
export interface RealEstateStatusOption {
  label: string
  value: string
}

/** 不动产入口动作键。 */
export type RealEstateActionKey =
  | 'realEstateOwnership'
  | 'realEstateUsage'
  | 'realEstateStatus'
  | 'realEstateDisposal'

/** 不动产入口判断结果。 */
export interface RealEstateActionGuard {
  disabled: boolean
  reason?: string
  latestActionText?: string
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

const REAL_ESTATE_TERMINAL_STATUSES = new Set(['5', '6'])

const REAL_ESTATE_ACTION_TYPE_MAP: Record<RealEstateActionKey, string> = {
  realEstateOwnership: 'REAL_ESTATE_OWNERSHIP_CHANGE',
  realEstateUsage: 'REAL_ESTATE_USAGE_CHANGE',
  realEstateStatus: 'REAL_ESTATE_STATUS_CHANGE',
  realEstateDisposal: 'REAL_ESTATE_DISPOSAL'
}

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
    assetStatus: String(query.assetStatus || '').trim() || undefined,
    latestActionType: String(query.latestActionType || '').trim() || undefined,
    latestActionLabel: String(query.latestActionLabel || '').trim() || undefined,
    latestDocStatus: String(query.latestDocStatus || '').trim() || undefined,
    latestActionTime: String(query.latestActionTime || '').trim() || undefined
  }
}

/** 自动打开弹窗时允许从 query 宽松恢复最小判断上下文，不强依赖 assetNo。 */
function buildLooseRouteContext(query: Record<string, unknown>): RealEstateRouteAssetContext {
  const assetId = Number(query.assetId)
  return {
    assetId: Number.isNaN(assetId) ? undefined : assetId,
    assetNo: String(query.assetNo || '').trim(),
    assetName: String(query.assetName || '').trim() || undefined,
    assetStatus: String(query.assetStatus || '').trim() || undefined,
    latestActionType: String(query.latestActionType || '').trim() || undefined,
    latestActionLabel: String(query.latestActionLabel || '').trim() || undefined,
    latestDocStatus: String(query.latestDocStatus || '').trim() || undefined,
    latestActionTime: String(query.latestActionTime || '').trim() || undefined
  }
}

/** 把后端原始单据状态映射到前端已有的审批流字典。 */
export function mapRealEstateStatusToWorkflow(status?: string) {
  if (status === 'pending') return 'IN_PROGRESS'
  if (status === 'approved' || status === 'completed') return 'COMPLETED'
  if (status === 'rejected') return 'REJECTED'
  return ''
}

/** 把最近动作翻译成适合在页头提示的短文案。 */
export function formatRealEstateLatestAction(context: RealEstateRouteAssetContext | null) {
  if (!context?.latestActionType) {
    return ''
  }
  const actionLabel = context.latestActionLabel || formatAssetTimelineAction(context.latestActionType)
  const statusLabel = formatAssetTimelineDocStatus(context.latestDocStatus)
  const timeText = context.latestActionTime ? `，${context.latestActionTime}` : ''
  return `${actionLabel} · ${statusLabel}${timeText}`
}

/** 基于当前状态和最近动作，判断不动产入口是否允许直接发起。 */
export function getRealEstateActionGuard(
  actionKey: RealEstateActionKey,
  context: RealEstateRouteAssetContext | null
): RealEstateActionGuard {
  const latestActionText = formatRealEstateLatestAction(context)
  if (!context) {
    return { disabled: false, latestActionText }
  }

  if (REAL_ESTATE_TERMINAL_STATUSES.has(context.assetStatus || '')) {
    return {
      disabled: true,
      reason: '当前资产已进入终态，不支持继续发起新的不动产动作。',
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
      reason: '当前资产已有注销/处置流程在途，请先等待审批结果。',
      latestActionText
    }
  }

  if (
    context.latestActionType === REAL_ESTATE_ACTION_TYPE_MAP[actionKey] &&
    context.latestDocStatus === 'pending'
  ) {
    return {
      disabled: true,
      reason: '当前相同动作仍在处理中，请先等待审批结果。',
      latestActionText
    }
  }

  return { disabled: false, latestActionText }
}

/** 只在显式带上 `openCreate=1` 且入口判断通过时自动打开新建弹窗。 */
export function shouldOpenCreateDialog(query: Record<string, unknown>) {
  if (String(query.openCreate || '').trim() !== '1') {
    return false
  }

  const actionKey = String(query.actionKey || '').trim() as RealEstateActionKey
  if (!actionKey) {
    return true
  }

  return !getRealEstateActionGuard(actionKey, parseAssetRouteQuery(query) || buildLooseRouteContext(query))
    .disabled
}

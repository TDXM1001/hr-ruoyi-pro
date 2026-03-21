export type RealEstateDetailTabName =
  | 'overview'
  | 'occupancy'
  | 'inspection'
  | 'rectification'
  | 'disposal'

const DETAIL_TAB_STORAGE_PREFIX = 'asset-real-estate-detail-tab:'

export const REAL_ESTATE_DETAIL_TABS: Array<{
  name: RealEstateDetailTabName
  label: string
  desc: string
}> = [
  {
    name: 'overview',
    label: '总览',
    desc: '总览页承接不动产的统一台账与权属视图，资产管理者先在这里确认当前状态和主档口径。'
  },
  {
    name: 'occupancy',
    label: '占用',
    desc: '占用页签只表达当前归属关系和历史交接轨迹，不直接承担流程流转。'
  },
  {
    name: 'inspection',
    label: '巡检',
    desc: '巡检页签聚焦任务事实、异常结果和任务级跳转，整改动作从这里发起。'
  },
  {
    name: 'rectification',
    label: '整改',
    desc: '整改页签沉淀责任、期限和完成状态，作为后续审批流的稳定挂载位。'
  },
  {
    name: 'disposal',
    label: '处置',
    desc: '处置页签只负责联动统一处置模块，避免不动产详情壳自己承载完整处置流程。'
  }
]

export function normalizeRealEstateDetailTab(value?: string | null): RealEstateDetailTabName {
  const target = String(value || '').trim()
  if (REAL_ESTATE_DETAIL_TABS.some((item) => item.name === target)) {
    return target as RealEstateDetailTabName
  }
  return 'overview'
}

export function resolveRealEstateDetailTabByPath(path?: string): RealEstateDetailTabName {
  if (path?.endsWith('/occupancy')) {
    return 'occupancy'
  }
  if (path?.endsWith('/inspection')) {
    return 'inspection'
  }
  if (path?.endsWith('/rectification')) {
    return 'rectification'
  }
  if (path?.endsWith('/disposal')) {
    return 'disposal'
  }
  return 'overview'
}

function buildStorageKey(assetId?: number | string) {
  if (assetId === undefined || assetId === null || assetId === '') {
    return ''
  }
  return `${DETAIL_TAB_STORAGE_PREFIX}${assetId}`
}

export function persistRealEstateDetailTab(assetId?: number | string, tab?: RealEstateDetailTabName) {
  if (typeof window === 'undefined') {
    return
  }
  const storageKey = buildStorageKey(assetId)
  if (!storageKey) {
    return
  }
  window.sessionStorage.setItem(storageKey, normalizeRealEstateDetailTab(tab))
}

export function readRealEstateDetailTab(assetId?: number | string) {
  if (typeof window === 'undefined') {
    return undefined
  }
  const storageKey = buildStorageKey(assetId)
  if (!storageKey) {
    return undefined
  }
  const cachedValue = window.sessionStorage.getItem(storageKey)
  return cachedValue ? normalizeRealEstateDetailTab(cachedValue) : undefined
}

import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'

export type TimeFilter = 'ALL' | '7D' | '30D' | '90D' | 'CUSTOM'
export type SortDirection = 'DESC' | 'ASC'
export type CompareFieldKey = 'useDeptName' | 'responsibleUserName' | 'locationName'
export type StatusFilter = 'ALL' | 'ACTIVE' | 'RELEASED'
export type GroupViewMode = 'LIST' | 'GROUPED' | 'ANNOTATION'
export type LinkedTabName = 'overview' | 'inspection' | 'rectification' | 'disposal'
export type LinkStatsWindow = '7D' | '30D'
export type ImportConflictPolicy = 'SKIP' | 'RENAME' | 'OVERWRITE'
export type ExportFieldKey =
  | 'occupancyNo'
  | 'occupancyStatus'
  | 'useDeptName'
  | 'responsibleUserName'
  | 'locationName'
  | 'startDate'
  | 'endDate'
  | 'changeReason'
  | 'releaseReason'

export interface OccupancyFilterState {
  statusFilter: StatusFilter
  timeFilter: TimeFilter
  sortDirection: SortDirection
  keyword: string
  customRangeDraftStart: string
  customRangeDraftEnd: string
  customRangeAppliedStart: string
  customRangeAppliedEnd: string
}

export interface ExportFieldOption {
  key: ExportFieldKey
  label: string
}

export interface ExportPresetOption {
  key: 'operations' | 'audit' | 'release'
  label: string
  fields: ExportFieldKey[]
}

export interface CustomExportPresetOption {
  key: string
  label: string
  fields: ExportFieldKey[]
  source: 'custom'
}

export type ExportPresetViewOption = (ExportPresetOption & { source: 'system' }) | CustomExportPresetOption
export type PresetCopySourceKey = 'current' | ExportPresetViewOption['key']
export type AnnotationTemplateKey = 'standard' | 'manager' | 'audit'
export type AnnotationCompareItemKey = 'status' | 'change' | 'release'
export type ImportConflictType = 'none' | 'system' | 'custom'
export type ItemImportConflictPolicy = ImportConflictPolicy | 'DEFAULT'
export type LinkStatsResetScope = 'EVENTS' | 'COUNTS' | 'ALL'

export interface OccupancyLinkStatEvent {
  targetKey: LinkedTabName
  targetLabel: string
  occurredAt: string
}

export interface OccupancyLinkStatsState {
  counts: Record<LinkedTabName, number>
  lastTargetKey: LinkedTabName | ''
  lastTargetLabel: string
  events: OccupancyLinkStatEvent[]
}

export interface PresetImportPreviewItem {
  key: string
  label: string
  fields: ExportFieldKey[]
  invalidFields: string[]
  conflictType: ImportConflictType
  resolvedLabel: string
  policyOverride: ItemImportConflictPolicy
}

export interface PresetImportInvalidItem {
  key: string
  label: string
  reason: string
  invalidFields: string[]
}

export interface PresetImportResultItem {
  label: string
  conflictType: ImportConflictType
  effectivePolicy: ImportConflictPolicy
  resolvedLabel: string
}

export interface PresetImportResultState {
  executedAt: string
  globalPolicy: ImportConflictPolicy
  importableCount: number
  invalidCount: number
  skippedCount: number
  renamedCount: number
  overwrittenCount: number
  appliedCount: number
  items: PresetImportResultItem[]
}

export interface LinkTrendItem {
  date: string
  label: string
  count: number
  topLabel: string
  barHeight: number
}

export interface TrendDrilldownState {
  date: string
  label: string
  count: number
}

export interface TrendFilterSnapshot {
  statusFilter: StatusFilter
  timeFilter: TimeFilter
  sortDirection: SortDirection
  keyword: string
  customRangeDraftStart: string
  customRangeDraftEnd: string
  customRangeAppliedStart: string
  customRangeAppliedEnd: string
}

export interface SavedTrendSnapshotState {
  key: string
  name: string
  savedAt: string
  linkStatsWindow: LinkStatsWindow
  drilldown: TrendDrilldownState | null
  filterState: TrendFilterSnapshot
}

export interface ImportPolicyTemplateItem {
  label: string
  conflictType: ImportConflictType
  effectivePolicy: ImportConflictPolicy
}

export interface ImportPolicyTemplateState {
  key: string
  name: string
  createdAt: string
  globalPolicy: ImportConflictPolicy
  itemPolicies: ImportPolicyTemplateItem[]
  lastAppliedAt?: string
  lastAppliedSummary?: string
  lastMatchedCount?: number
}

export interface LinkResetLogState {
  key: string
  executedAt: string
  scope: LinkStatsResetScope
  summary: string
}

export type GovernanceActivityType = 'TEMPLATE' | 'SNAPSHOT' | 'RESET' | 'EXPORT'

export interface GovernanceActivityState {
  key: string
  type: GovernanceActivityType
  label: string
  target: string
  summary: string
  executedAt: string
}

export interface GovernanceExportMetaState {
  exportedAt: string
  fileName: string
}

export type ResetLogFilterScope = 'ALL_RECORDS' | LinkStatsResetScope

export const compareFieldLabels: Record<CompareFieldKey, string> = {
  useDeptName: '使用部门',
  responsibleUserName: '责任人',
  locationName: '使用位置'
}

export const exportFieldOptions: ExportFieldOption[] = [
  { key: 'occupancyNo', label: '占用单号' },
  { key: 'occupancyStatus', label: '占用状态' },
  { key: 'useDeptName', label: '使用部门' },
  { key: 'responsibleUserName', label: '责任人' },
  { key: 'locationName', label: '使用位置' },
  { key: 'startDate', label: '占用起始' },
  { key: 'endDate', label: '释放时间' },
  { key: 'changeReason', label: '发起/变更原因' },
  { key: 'releaseReason', label: '释放原因' }
]

export const exportPresetOptions: ExportPresetOption[] = [
  {
    key: 'operations',
    label: '运营摘要',
    fields: ['occupancyNo', 'occupancyStatus', 'useDeptName', 'responsibleUserName', 'locationName']
  },
  {
    key: 'audit',
    label: '审计复盘',
    fields: [...exportFieldOptions.map((item) => item.key)]
  },
  {
    key: 'release',
    label: '释放分析',
    fields: ['occupancyNo', 'occupancyStatus', 'useDeptName', 'locationName', 'endDate', 'changeReason', 'releaseReason']
  }
]

export const annotationTemplateOptions: Array<{ key: AnnotationTemplateKey; label: string }> = [
  { key: 'standard', label: '标准模板' },
  { key: 'manager', label: '管理视角' },
  { key: 'audit', label: '审计视角' }
]

export const importConflictPolicyOptions: Array<{ key: ImportConflictPolicy; label: string }> = [
  { key: 'SKIP', label: '跳过同名' },
  { key: 'RENAME', label: '自动改名' },
  { key: 'OVERWRITE', label: '覆盖同名' }
]

export const tabLinkOptions: { key: LinkedTabName; label: string }[] = [
  { key: 'overview', label: '回总览核对主档' },
  { key: 'inspection', label: '看巡检联动' },
  { key: 'rectification', label: '看整改进展' },
  { key: 'disposal', label: '看处置关联' }
]

export const linkStatsWindowOptions: Array<{ key: LinkStatsWindow; label: string }> = [
  { key: '7D', label: '近 7 天' },
  { key: '30D', label: '近 30 天' }
]

export const linkStatsResetScopeOptions: Array<{ key: LinkStatsResetScope; label: string }> = [
  { key: 'EVENTS', label: '只重置趋势' },
  { key: 'COUNTS', label: '只重置来源' },
  { key: 'ALL', label: '全部重置' }
]

export const defaultExportFieldKeys: ExportFieldKey[] = exportFieldOptions.map((item) => item.key)

export const defaultFilterState: OccupancyFilterState = {
  statusFilter: 'ALL',
  timeFilter: 'ALL',
  sortDirection: 'DESC',
  keyword: '',
  customRangeDraftStart: '',
  customRangeDraftEnd: '',
  customRangeAppliedStart: '',
  customRangeAppliedEnd: ''
}

export const parseDateValue = (value?: string) => {
  if (!value) {
    return undefined
  }
  const normalized = /^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00` : value
  const parsed = new Date(normalized)
  return Number.isNaN(parsed.getTime()) ? undefined : parsed
}

// 趋势图按浏览器本地自然日分桶，避免 UTC 字符串把当日联动偏移到前一天。
export const formatLocalDateKey = (date: Date) => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const formatLocalDateTime = (date: Date) => {
  const dateKey = formatLocalDateKey(date)
  const hours = `${date.getHours()}`.padStart(2, '0')
  const minutes = `${date.getMinutes()}`.padStart(2, '0')
  const seconds = `${date.getSeconds()}`.padStart(2, '0')
  return `${dateKey} ${hours}:${minutes}:${seconds}`
}

export const getDisplayValue = (value?: string) => {
  const text = String(value || '').trim()
  return text || '-'
}

export const getRecordKey = (record: AssetRealEstateOccupancyRecord) => {
  return String(record.occupancyId || record.occupancyNo || record.startDate || 'unknown')
}

export const buildCompareItems = (
  baseSource: Record<string, any>,
  compareSource: Record<string, any>
) => {
  return (Object.keys(compareFieldLabels) as CompareFieldKey[]).map((key) => {
    const baseValue = getDisplayValue(baseSource?.[key])
    const compareValue = getDisplayValue(compareSource?.[key])
    return {
      key,
      label: compareFieldLabels[key],
      baseValue,
      compareValue,
      changed: baseValue !== compareValue
    }
  })
}

export const getStatusLabel = (status?: string) => {
  const mapper: Record<string, string> = {
    ACTIVE: '有效占用',
    RELEASED: '已释放'
  }
  return mapper[String(status || '').toUpperCase()] || status || '-'
}

export const buildAnnotationStatusNote = (
  record: AssetRealEstateOccupancyRecord,
  templateKey: AnnotationTemplateKey = 'standard'
) => {
  const isActive = String(record.occupancyStatus || '').toUpperCase() === 'ACTIVE'
  if (templateKey === 'manager') {
    return isActive
      ? '管理视角：当前占用仍在持续，请优先核对责任归属和主档同步状态。'
      : '管理视角：该条轨迹已经释放，可作为本次占用结束与重新分配的依据。'
  }
  if (templateKey === 'audit') {
    return isActive
      ? '审计视角：当前轨迹仍为有效占用，应作为最近一次占用依据。'
      : '审计视角：该轨迹已释放，应作为历史留痕和释放凭据记录。'
  }
  return isActive
    ? '该轨迹仍是当前有效占用，主档应以这条占用记录为准。'
    : '该轨迹已经释放，仅保留为历史留痕，不再承接变更或释放动作。'
}

export const buildAnnotationChangeNote = (
  record: AssetRealEstateOccupancyRecord,
  templateKey: AnnotationTemplateKey = 'standard'
) => {
  const reason = record.changeReason || '-'
  if (templateKey === 'manager') {
    return `管理视角：占用依据 ${reason}`
  }
  if (templateKey === 'audit') {
    return `审计视角：占用凭据 ${reason}`
  }
  return reason
}

export const buildAnnotationReleaseNote = (
  record: AssetRealEstateOccupancyRecord,
  templateKey: AnnotationTemplateKey = 'standard'
) => {
  const reason = record.releaseReason || '-'
  if (templateKey === 'manager') {
    return `管理视角：释放结论 ${reason}`
  }
  if (templateKey === 'audit') {
    return `审计视角：释放凭据 ${reason}`
  }
  return reason
}

export const buildAnnotationPreviewItems = (record?: AssetRealEstateOccupancyRecord) => {
  if (!record) {
    return []
  }
  return [
    { label: '状态说明样例', value: buildAnnotationStatusNote(record) },
    { label: '占用批注样例', value: buildAnnotationChangeNote(record) },
    { label: '释放批注样例', value: buildAnnotationReleaseNote(record) }
  ]
}

export const buildImportConflictLabel = (conflictType: ImportConflictType) => {
  const mapper: Record<ImportConflictType, string> = {
    none: '可直接导入',
    system: '系统预设重名',
    custom: '自定义预设重名'
  }
  return mapper[conflictType]
}

export const buildImportPolicyLabel = (policy: ImportConflictPolicy) => {
  const mapper: Record<ImportConflictPolicy, string> = {
    SKIP: '跳过',
    RENAME: '改名',
    OVERWRITE: '覆盖'
  }
  return mapper[policy]
}

export const buildResetScopeLabel = (scope: LinkStatsResetScope) => {
  const mapper: Record<LinkStatsResetScope, string> = {
    EVENTS: '只重置趋势',
    COUNTS: '只重置来源',
    ALL: '全部重置'
  }
  return mapper[scope]
}

export const buildGovernanceActivityTypeLabel = (type: GovernanceActivityType) => {
  const mapper: Record<GovernanceActivityType, string> = {
    TEMPLATE: '模板',
    SNAPSHOT: '快照',
    RESET: '重置',
    EXPORT: '导出'
  }
  return mapper[type]
}

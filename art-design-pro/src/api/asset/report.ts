import request from '@/utils/http'

/** 资产类型汇总项。 */
export interface AssetReportTypeSummary {
  assetType?: string
  assetCount?: number
  originalValueTotal?: number
}

/** 资产状态汇总项。 */
export interface AssetReportStatusSummary {
  assetStatus?: string
  assetCount?: number
  originalValueTotal?: number
}

/** 部门汇总项。 */
export interface AssetReportDeptSummary {
  deptId?: number
  deptName?: string
  assetCount?: number
  originalValueTotal?: number
}

/** 资产报表汇总结构。 */
export interface AssetReportSummary {
  totalAssetCount?: number
  totalOriginalValue?: number
  fixedAssetCount?: number
  fixedAssetOriginalValue?: number
  realEstateCount?: number
  realEstateOriginalValue?: number
  typeSummaries: AssetReportTypeSummary[]
  statusSummaries: AssetReportStatusSummary[]
  deptSummaries: AssetReportDeptSummary[]
}

/** 单条预警记录。 */
export interface AssetWarningItem {
  assetId?: number
  assetNo?: string
  assetName?: string
  assetType?: string
  /** 资产状态：描述资产本体当前状态。 */
  assetStatus?: string
  /** 单据状态：描述业务单据当前处理阶段。 */
  status?: string
  /** 流程状态：描述审批流当前推进状态。 */
  wfStatus?: string
  /** 归档状态：描述资产是否已从日常台账归档。 */
  archiveStatus?: string
  deptId?: number
  deptName?: string
  businessNo?: string
  businessType?: string
  handlerName?: string
  warningCode?: string
  warningLabel?: string
  detail?: string
  eventTime?: string
  dueDate?: string
}

/** 资产预警集合。 */
export interface AssetWarningCollection {
  idleAssets: AssetWarningItem[]
  maintenanceAssets: AssetWarningItem[]
  pendingApprovalItems: AssetWarningItem[]
  landTermExpiringAssets: AssetWarningItem[]
}

/** 资产预警查询参数。 */
export interface AssetWarningQuery {
  landTermWithinDays?: number
}

/** 查询资产报表汇总。 */
export function getAssetReportSummary() {
  return request.get<AssetReportSummary>({
    url: '/asset/report/summary'
  })
}

/** 查询资产预警集合。 */
export function getAssetWarnings(params?: AssetWarningQuery) {
  return request.get<AssetWarningCollection>({
    url: '/asset/report/warnings',
    params
  })
}

import http from '@/utils/http'

/**
 * 资产树形选项。
 */
export interface AssetTreeOption {
  id: number
  label: string
  disabled?: boolean
  children?: AssetTreeOption[]
}

/**
 * 责任人选项。
 */
export interface AssetUserOption {
  value: number
  label: string
}

/**
 * 资产生命周期变更记录。
 */
export interface AssetChangeLogRecord {
  logId?: number
  assetId?: number
  bizType?: string
  bizId?: number
  beforeStatus?: string
  afterStatus?: string
  operateBy?: string
  operateTime?: string
  changeDesc?: string
}

/**
 * 资产交接记录。
 */
export interface AssetHandoverRecord {
  handoverItemId?: number
  handoverOrderId?: number
  handoverNo?: string
  handoverType?: string
  handoverDate?: string
  beforeStatus?: string
  afterStatus?: string
  fromDeptName?: string
  fromUserName?: string
  fromLocationName?: string
  toDeptName?: string
  toUserName?: string
  toLocationName?: string
}

/**
 * 不动产占用记录。
 */
export interface AssetRealEstateOccupancyRecord {
  occupancyId?: number
  occupancyNo?: string
  assetId?: number
  useDeptId?: number
  useDeptName?: string
  responsibleUserId?: number
  responsibleUserName?: string
  locationName?: string
  startDate?: string
  endDate?: string
  occupancyStatus?: string
  changeReason?: string
  releaseReason?: string
}

/**
 * 资产盘点记录。
 */
export interface AssetInventoryRecord {
  itemId?: number
  taskId?: number
  taskNo?: string
  taskName?: string
  assetCode?: string
  inventoryResult?: string
  followUpAction?: string
  processStatus?: string
  followUpBizId?: number
  checkedBy?: string
  checkedTime?: string
  resultDesc?: string
}

/**
 * 资产整改记录。
 */
export interface AssetRectificationRecord {
  rectificationId?: number
  rectificationNo?: string
  assetId?: number
  assetCode?: string
  assetName?: string
  taskId?: number
  taskNo?: string
  taskName?: string
  inventoryItemId?: number
  rectificationStatus?: string
  issueType?: string
  issueDesc?: string
  responsibleDeptId?: number
  responsibleDeptName?: string
  responsibleUserId?: number
  responsibleUserName?: string
  deadlineDate?: string
  completedTime?: string
  completionDesc?: string
  acceptanceRemark?: string
  approvalStatus?: string
  approvalSubmittedTime?: string
  approvalFinishedTime?: string
  createTime?: string
  remark?: string
}

/**
 * 资产处置记录。
 */
export interface AssetDisposalRecord {
  disposalId?: number
  disposalNo?: string
  disposalType?: string
  disposalStatus?: string
  disposalDate?: string
  disposalAmount?: number
  confirmedBy?: string
  confirmedTime?: string
  disposalReason?: string
}

/**
 * 资产生命周期聚合详情。
 */
export interface AssetLedgerLifecycleDetail {
  ledger?: Record<string, any>
  occupancyRecords?: AssetRealEstateOccupancyRecord[]
  handoverRecords?: AssetHandoverRecord[]
  inventoryRecords?: AssetInventoryRecord[]
  rectificationOrders?: AssetRectificationRecord[]
  disposalRecords?: AssetDisposalRecord[]
  changeLogs?: AssetChangeLogRecord[]
}

/**
 * 查询资产台账列表。
 */
export function listAssetLedger(query?: any) {
  return http.request({ url: '/asset/ledger/list', method: 'get', params: query })
}

/**
 * 查询资产台账详情。
 */
export function getAssetLedger(assetId: number | string) {
  return http.request({ url: '/asset/ledger/' + assetId, method: 'get' })
}

/**
 * 查询资产生命周期聚合详情。
 */
export function getAssetLedgerLifecycle(assetId: number | string) {
  return http.request<AssetLedgerLifecycleDetail>({
    url: `/asset/ledger/${assetId}/lifecycle`,
    method: 'get'
  })
}

/**
 * 新增资产台账。
 */
export function addAssetLedger(data: any) {
  return http.request({ url: '/asset/ledger', method: 'post', data })
}

/**
 * 修改资产台账。
 */
export function updateAssetLedger(data: any) {
  return http.request({ url: '/asset/ledger', method: 'put', data })
}

/**
 * 导出资产台账。
 */
export function exportAssetLedger(data?: any) {
  return http.request<Blob>({
    url: '/asset/ledger/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

/**
 * 获取下一条建议资产编码。
 */
export function getNextAssetCode() {
  return http
    .request({
      url: '/asset/ledger/nextCode',
      method: 'get'
    })
    .then((response) => normalizeAssetCodeResponse(response))
}

function normalizeAssetCodeResponse(response: any) {
  if (typeof response === 'string') {
    return response
  }
  if (typeof response?.data === 'string') {
    return response.data
  }
  if (typeof response?.data?.data === 'string') {
    return response.data.data
  }
  if (typeof response?.msg === 'string') {
    return response.msg
  }
  if (typeof response?.data?.msg === 'string') {
    return response.data.msg
  }
  return ''
}

/**
 * 查询资产分类树。
 */
export function getAssetCategoryTree() {
  return http.request<AssetTreeOption[]>({
    url: '/asset/ledger/categoryTree',
    method: 'get'
  })
}

/**
 * 查询部门树。
 */
export function getAssetDeptTree() {
  return http.request<AssetTreeOption[]>({
    url: '/asset/ledger/deptTree',
    method: 'get'
  })
}

/**
 * 远程搜索责任人。
 */
export function listAssetResponsibleUsers(query?: { keyword?: string }) {
  return http.request<AssetUserOption[]>({
    url: '/asset/ledger/responsibleUsers',
    method: 'get',
    params: query
  })
}

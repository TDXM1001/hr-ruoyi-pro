import http from '@/utils/http'
import type {
  AssetLedgerLifecycleDetail,
  AssetRealEstateOccupancyRecord,
  AssetRectificationRecord,
  AssetTreeOption,
  AssetUserOption
} from '@/api/asset/ledger'

export type {
  AssetRealEstateOccupancyRecord,
  AssetTreeOption,
  AssetUserOption
} from '@/api/asset/ledger'

/**
 * 不动产档案查询参数。
 */
export interface AssetRealEstateQuery {
  assetId?: number
  assetCode?: string
  assetName?: string
  ownershipCertNo?: string
  landUseType?: string
  categoryId?: number
  ownerDeptId?: number
  useDeptId?: number
  responsibleUserId?: number
  assetStatus?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 不动产档案详情。
 */
export interface AssetRealEstateDetail {
  profileId?: number
  assetId?: number
  assetCode?: string
  assetName?: string
  assetType?: string
  categoryId?: number
  categoryName?: string
  specModel?: string
  serialNo?: string
  assetStatus?: string
  sourceType?: string
  acquireType?: string
  ownerDeptId?: number
  ownerDeptName?: string
  useDeptId?: number
  useDeptName?: string
  responsibleUserId?: number
  responsibleUserName?: string
  locationName?: string
  originalValue?: number
  acquisitionDate?: string
  enableDate?: string
  lastInventoryDate?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  ownershipCertNo?: string
  landUseType?: string
  buildingArea?: number
  remark?: string
}

/**
 * 不动产占用登记/变更入参。
 */
export interface AssetRealEstateOccupancyPayload {
  useDeptId?: number
  responsibleUserId?: number
  locationName?: string
  startDate?: string
  changeReason?: string
}

/**
 * 不动产占用释放入参。
 */
export interface AssetRealEstateOccupancyReleasePayload {
  endDate?: string
  releaseReason?: string
}

/**
 * 不动产整改单入参。
 */
export interface AssetRealEstateRectificationPayload {
  rectificationId?: number
  assetId?: number
  taskId?: number
  inventoryItemId?: number
  rectificationStatus?: 'PENDING' | 'COMPLETED' | string
  issueType?: string
  issueDesc?: string
  responsibleDeptId?: number
  responsibleUserId?: number
  deadlineDate?: string
  remark?: string
}

export interface AssetRealEstateRectificationCompletePayload {
  completionDesc: string
  acceptanceRemark?: string
}

export interface AssetRealEstateRectificationApprovalActionPayload {
  opinion: string
}

export interface AssetRealEstateRectificationApprovalRecord {
  approvalRecordId?: number
  rectificationId?: number
  assetId?: number
  approvalStatus?: string
  opinion?: string
  operateBy?: string
  operateTime?: string
}

export function getRealEstateList(params?: AssetRealEstateQuery) {
  return http.request({
    url: '/asset/real-estate/list',
    method: 'get',
    params
  })
}

export function getRealEstateDetail(assetId: number | string) {
  return http.request<AssetRealEstateDetail>({
    url: `/asset/real-estate/${assetId}`,
    method: 'get'
  })
}

export function getRealEstateLifecycle(assetId: number | string) {
  return http.request<AssetLedgerLifecycleDetail>({
    url: `/asset/real-estate/${assetId}/lifecycle`,
    method: 'get'
  })
}

export function addRealEstate(data: Partial<AssetRealEstateDetail>) {
  return http.request({
    url: '/asset/real-estate',
    method: 'post',
    data
  })
}

export function updateRealEstate(data: Partial<AssetRealEstateDetail>) {
  return http.request({
    url: '/asset/real-estate',
    method: 'put',
    data
  })
}

export function getRealEstateCategoryTree() {
  return http.request<AssetTreeOption[]>({
    url: '/asset/real-estate/categoryTree',
    method: 'get'
  })
}

export function getRealEstateDeptTree() {
  return http.request<AssetTreeOption[]>({
    url: '/asset/real-estate/deptTree',
    method: 'get'
  })
}

export function listRealEstateResponsibleUsers(query?: { keyword?: string }) {
  return http.request<AssetUserOption[]>({
    url: '/asset/real-estate/responsibleUsers',
    method: 'get',
    params: query
  })
}

export function getNextRealEstateCode() {
  return http
    .request({
      url: '/asset/real-estate/nextCode',
      method: 'get'
    })
    .then((response) => normalizeAssetCodeResponse(response))
}

export function listRealEstateOccupancies(assetId: number | string) {
  return http.request<AssetRealEstateOccupancyRecord[]>({
    url: `/asset/real-estate/${assetId}/occupancies`,
    method: 'get'
  })
}

export function addRealEstateOccupancy(
  assetId: number | string,
  data: AssetRealEstateOccupancyPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/occupancies`,
    method: 'post',
    data
  })
}

export function changeRealEstateOccupancy(
  assetId: number | string,
  occupancyId: number | string,
  data: AssetRealEstateOccupancyPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/occupancies/${occupancyId}/change`,
    method: 'post',
    data
  })
}

export function releaseRealEstateOccupancy(
  assetId: number | string,
  occupancyId: number | string,
  data: AssetRealEstateOccupancyReleasePayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/occupancies/${occupancyId}/release`,
    method: 'post',
    data
  })
}

export function listRealEstateRectifications(assetId: number | string) {
  return http.request<AssetRectificationRecord[]>({
    url: `/asset/real-estate/${assetId}/rectifications`,
    method: 'get'
  })
}

export function getRealEstateRectification(assetId: number | string, rectificationId: number | string) {
  return http.request<AssetRectificationRecord>({
    url: `/asset/real-estate/${assetId}/rectifications/${rectificationId}`,
    method: 'get'
  })
}

export function addRealEstateRectification(
  assetId: number | string,
  data: AssetRealEstateRectificationPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/rectifications`,
    method: 'post',
    data
  })
}

export function updateRealEstateRectification(
  assetId: number | string,
  data: AssetRealEstateRectificationPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/rectifications`,
    method: 'put',
    data
  })
}

export function completeRealEstateRectification(
  assetId: number | string,
  rectificationId: number | string,
  data: AssetRealEstateRectificationCompletePayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/rectifications/${rectificationId}/complete`,
    method: 'post',
    data
  })
}

export function listRealEstateRectificationApprovalRecords(
  assetId: number | string,
  rectificationId: number | string
) {
  return http.request<AssetRealEstateRectificationApprovalRecord[]>({
    url: `/asset/real-estate/${assetId}/rectifications/${rectificationId}/approval-records`,
    method: 'get'
  })
}

export function submitRealEstateRectificationApproval(
  assetId: number | string,
  rectificationId: number | string,
  data: AssetRealEstateRectificationApprovalActionPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/rectifications/${rectificationId}/submit-approval`,
    method: 'post',
    data
  })
}

export function approveRealEstateRectification(
  assetId: number | string,
  rectificationId: number | string,
  data: AssetRealEstateRectificationApprovalActionPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/rectifications/${rectificationId}/approve`,
    method: 'post',
    data
  })
}

export function rejectRealEstateRectificationApproval(
  assetId: number | string,
  rectificationId: number | string,
  data: AssetRealEstateRectificationApprovalActionPayload
) {
  return http.request({
    url: `/asset/real-estate/${assetId}/rectifications/${rectificationId}/reject`,
    method: 'post',
    data
  })
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

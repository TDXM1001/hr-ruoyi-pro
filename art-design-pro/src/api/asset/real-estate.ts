import http from '@/utils/http'
import type { AssetLedgerLifecycleDetail, AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'

export type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'

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
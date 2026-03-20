import http from '@/utils/http'

/**
 * 不动产权属档案查询参数。
 */
export interface AssetRealEstateQuery {
  assetId?: number
  ownershipCertNo?: string
  landUseType?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 不动产权属档案详情。
 */
export interface AssetRealEstateProfile {
  profileId?: number
  assetId?: number
  ownershipCertNo?: string
  landUseType?: string
  buildingArea?: number
  remark?: string
}

/**
 * 查询不动产权属档案列表。
 * @param params 查询条件
 * @returns 分页数据
 */
export function getRealEstateList(params?: AssetRealEstateQuery) {
  return http.request({
    url: '/asset/real-estate/list',
    method: 'get',
    params
  })
}

/**
 * 查询不动产权属档案详情。
 * @param assetId 资产ID
 * @returns 档案详情
 */
export function getRealEstateDetail(assetId: number | string) {
  return http.request<AssetRealEstateProfile>({
    url: `/asset/real-estate/${assetId}`,
    method: 'get'
  })
}


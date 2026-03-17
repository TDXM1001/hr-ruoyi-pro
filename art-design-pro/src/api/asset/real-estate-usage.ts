import type { AssetBusinessOrderBase, AssetRef } from '@/types/asset'
import request from '@/utils/http'

/** 不动产用途变更查询参数。 */
export interface UsageChangeQuery {
  usageChangeNo?: string
  assetNo?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

/** 新增不动产用途变更请求。 */
export interface CreateUsageChangeReq extends Pick<AssetRef, 'assetId' | 'assetNo'> {
  targetLandUse?: string
  targetBuildingUse?: string
  reason: string
}

/** 不动产用途变更台账最小行结构。 */
export interface UsageChangeItem extends AssetBusinessOrderBase {
  usageChangeNo: string
  assetId: number
  assetNo: string
  status: string
  wfStatus?: string
  oldLandUse?: string
  targetLandUse?: string
  oldBuildingUse?: string
  targetBuildingUse?: string
  reason?: string
  applyUserId?: number
  createTime?: string
}

/** 查询不动产用途变更列表。 */
export function listUsageChange(params?: UsageChangeQuery) {
  return request.get<UsageChangeItem[]>({
    url: '/asset/real-estate/usage/list',
    params
  })
}

/** 查询不动产用途变更详情。 */
export function getUsageChangeDetail(usageChangeNo: string) {
  return request.get<UsageChangeItem>({
    url: `/asset/real-estate/usage/${usageChangeNo}`
  })
}

/** 提交不动产用途变更申请。 */
export function createUsageChange(data: CreateUsageChangeReq) {
  return request.post({
    url: '/asset/real-estate/usage',
    data
  })
}

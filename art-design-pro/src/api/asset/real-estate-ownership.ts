import request from '@/utils/http'

/** 不动产权属变更查询参数。 */
export interface OwnershipChangeQuery {
  ownershipChangeNo?: string
  assetNo?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

/** 新增不动产权属变更请求。 */
export interface CreateOwnershipChangeReq {
  assetId?: number
  assetNo?: string
  targetRightsHolder: string
  targetPropertyCertNo?: string
  targetRegistrationDate?: string
  reason: string
}

/** 不动产权属变更台账最小行结构。 */
export interface OwnershipChangeItem {
  ownershipChangeNo: string
  assetId?: number
  assetNo: string
  status: string
  oldRightsHolder?: string
  targetRightsHolder?: string
  oldPropertyCertNo?: string
  targetPropertyCertNo?: string
  oldRegistrationDate?: string
  targetRegistrationDate?: string
  reason?: string
  applyUserId?: number
  createTime?: string
}

/** 查询不动产权属变更列表。 */
export function listOwnershipChange(params?: OwnershipChangeQuery) {
  return request.get<OwnershipChangeItem[]>({
    url: '/asset/real-estate/ownership/list',
    params
  })
}

/** 查询不动产权属变更详情。 */
export function getOwnershipChangeDetail(ownershipChangeNo: string) {
  return request.get<OwnershipChangeItem>({
    url: `/asset/real-estate/ownership/${ownershipChangeNo}`
  })
}

/** 提交不动产权属变更申请。 */
export function createOwnershipChange(data: CreateOwnershipChangeReq) {
  return request.post({
    url: '/asset/real-estate/ownership',
    data
  })
}

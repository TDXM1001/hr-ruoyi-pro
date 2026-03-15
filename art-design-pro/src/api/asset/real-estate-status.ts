import request from '@/utils/http'

/** 不动产状态变更查询参数。 */
export interface StatusChangeQuery {
  statusChangeNo?: string
  assetNo?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

/** 新增不动产状态变更请求。 */
export interface CreateStatusChangeReq {
  assetId?: number
  assetNo?: string
  targetAssetStatus: string
  reason: string
}

/** 不动产状态变更台账最小行结构。 */
export interface StatusChangeItem {
  statusChangeNo: string
  assetId?: number
  assetNo: string
  status: string
  oldAssetStatus?: string
  targetAssetStatus?: string
  reason?: string
  applyUserId?: number
  createTime?: string
}

/** 查询不动产状态变更列表。 */
export function listStatusChange(params?: StatusChangeQuery) {
  return request.get<StatusChangeItem[]>({
    url: '/asset/real-estate/status/list',
    params
  })
}

/** 查询不动产状态变更详情。 */
export function getStatusChangeDetail(statusChangeNo: string) {
  return request.get<StatusChangeItem>({
    url: `/asset/real-estate/status/${statusChangeNo}`
  })
}

/** 提交不动产状态变更申请。 */
export function createStatusChange(data: CreateStatusChangeReq) {
  return request.post({
    url: '/asset/real-estate/status',
    data
  })
}

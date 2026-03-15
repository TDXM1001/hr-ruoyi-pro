import request from '@/utils/http'

/** 不动产注销/处置查询参数。 */
export interface RealEstateDisposalQuery {
  disposalNo?: string
  assetNo?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

/** 新增不动产注销/处置请求。 */
export interface CreateRealEstateDisposalReq {
  assetId?: number
  assetNo?: string
  disposalType?: string
  targetAssetStatus?: string
  reason: string
}

/** 不动产注销/处置台账最小行结构。 */
export interface RealEstateDisposalItem {
  disposalNo: string
  assetId?: number
  assetNo: string
  status: string
  disposalType?: string
  oldAssetStatus?: string
  targetAssetStatus?: string
  reason?: string
  applyUserId?: number
  createTime?: string
}

/** 查询不动产注销/处置列表。 */
export function listRealEstateDisposal(params?: RealEstateDisposalQuery) {
  return request.get<RealEstateDisposalItem[]>({
    url: '/asset/real-estate/disposal/list',
    params
  })
}

/** 查询不动产注销/处置详情。 */
export function getRealEstateDisposalDetail(disposalNo: string) {
  return request.get<RealEstateDisposalItem>({
    url: `/asset/real-estate/disposal/${disposalNo}`
  })
}

/** 提交不动产注销/处置申请。 */
export function createRealEstateDisposal(data: CreateRealEstateDisposalReq) {
  return request.post({
    url: '/asset/real-estate/disposal',
    data
  })
}

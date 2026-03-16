import request from '@/utils/http'
import type { AssetBusinessOrderBase, FixedAssetDisposalType } from '@/types/asset'

/**
 * 处置台账列表查询参数
 */
export interface DisposalQuery {
  disposalNo?: string
  assetNo?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 发起处置申请请求
 *
 * 和维修台账保持同一口径：创建时必须显式携带 `assetId` 与 `assetNo`。
 */
export interface ApplyDisposalReq {
  assetId: number
  assetNo: string
  disposalType: FixedAssetDisposalType
  reason: string
}

/**
 * 处置台账最小行结构。
 */
export interface AssetDisposalItem extends AssetBusinessOrderBase {
  disposalNo: string
  assetId: number
  assetNo: string
  disposalType?: FixedAssetDisposalType
  reason?: string
  status: number
  wfStatus?: string
  applyUserId?: number
  createTime?: string
}

/**
 * 查询处置记录列表
 */
export function listDisposal(params?: DisposalQuery) {
  return request.get<AssetDisposalItem[]>({
    url: '/asset/disposal/list',
    params
  })
}

/**
 * 查询处置详情
 */
export function getDisposal(disposalNo: string) {
  return request.get<AssetDisposalItem>({
    url: `/asset/disposal/${disposalNo}`
  })
}

/**
 * 提交处置申请
 */
export function applyDisposal(data: ApplyDisposalReq) {
  return request.post({
    url: '/asset/disposal',
    data
  })
}

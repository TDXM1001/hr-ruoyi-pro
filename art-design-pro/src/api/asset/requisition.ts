import request from '@/utils/http'

/**
 * 领用台账列表查询参数
 */
export interface RequisitionQuery {
  requisitionNo?: string
  assetNo?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 发起领用请求
 */
export interface ApplyRequisitionReq {
  assetNo: string
  reason: string
}

/**
 * 查询领用记录列表
 * @param params 查询参数
 */
export function listRequisition(params?: RequisitionQuery) {
  return request.get<any[]>({
    url: '/asset/requisition/list',
    params
  })
}

/**
 * 提交资产领用申请
 * @param data 申请数据
 */
export function applyRequisition(data: ApplyRequisitionReq) {
  return request.post({
    url: '/asset/requisition',
    data
  })
}

/**
 * 提交资产归还
 * @param requisitionNo 领用单号
 */
export function returnAsset(requisitionNo: string) {
  return request.post({
    url: `/asset/requisition/return/${requisitionNo}`
  })
}

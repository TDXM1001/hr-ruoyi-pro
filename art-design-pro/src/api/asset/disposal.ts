import http from '@/utils/http'

/**
 * 资产处置查询参数
 */
export interface AssetDisposalQuery {
  disposalNo?: string
  assetId?: number
  disposalType?: string
  disposalStatus?: string
  'params[beginTime]'?: string
  'params[endTime]'?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 资产处置确认参数
 */
export interface AssetDisposalPayload {
  assetId: number
  disposalType: string
  disposalReason: string
  disposalDate: string
  disposalAmount?: number
  financeConfirmFlag?: '0' | '1' | string
  remark?: string
}

/**
 * 审批动作参数
 */
export interface AssetDisposalApprovalPayload {
  opinion: string
}

/**
 * 查询处置记录列表
 * @param params 查询参数
 * @returns 处置记录分页数据
 */
export function listAssetDisposal(params?: AssetDisposalQuery) {
  return http.request({ url: '/asset/disposal/list', method: 'get', params })
}

/**
 * 查询处置记录详情
 * @param disposalId 处置ID
 * @returns 处置详情
 */
export function getAssetDisposal(disposalId: number | string) {
  return http.request({ url: `/asset/disposal/${disposalId}`, method: 'get' })
}

/**
 * 新增（确认）资产处置
 * @param data 处置参数
 * @returns 处置ID
 */
export function addAssetDisposal(data: AssetDisposalPayload) {
  return http.request({ url: '/asset/disposal', method: 'post', data })
}

/**
 * 审批通过处置单
 * @param disposalId 处置单ID
 * @param data 审批参数
 * @returns 结果
 */
export function approveAssetDisposal(disposalId: number | string, data: AssetDisposalApprovalPayload) {
  return http.request({ url: `/asset/disposal/${disposalId}/approve`, method: 'post', data })
}

/**
 * 审批驳回处置单
 * @param disposalId 处置单ID
 * @param data 审批参数
 * @returns 结果
 */
export function rejectAssetDisposal(disposalId: number | string, data: AssetDisposalApprovalPayload) {
  return http.request({ url: `/asset/disposal/${disposalId}/reject`, method: 'post', data })
}

/**
 * 查询处置审批轨迹
 * @param disposalId 处置单ID
 * @returns 审批轨迹
 */
export function listAssetDisposalApprovals(disposalId: number | string) {
  return http.request({ url: `/asset/disposal/${disposalId}/approvals`, method: 'get' })
}

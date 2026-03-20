import http from '@/utils/http'

/**
 * 资产树形选择节点。
 */
export interface AssetTreeOption {
  id: number
  label: string
  disabled?: boolean
  children?: AssetTreeOption[]
}

/**
 * 责任人下拉选项。
 */
export interface AssetUserOption {
  value: number
  label: string
}

/**
 * 查询资产台账列表。
 * @param query 查询条件
 * @returns 台账分页数据
 */
export function listAssetLedger(query?: any) {
  return http.request({ url: '/asset/ledger/list', method: 'get', params: query })
}

/**
 * 查询资产台账详情。
 * @param assetId 资产ID
 * @returns 台账详情
 */
export function getAssetLedger(assetId: number | string) {
  return http.request({ url: '/asset/ledger/' + assetId, method: 'get' })
}

/**
 * 新增资产台账。
 * @param data 台账数据
 * @returns 新增结果
 */
export function addAssetLedger(data: any) {
  return http.request({ url: '/asset/ledger', method: 'post', data })
}

/**
 * 修改资产台账。
 * @param data 台账数据
 * @returns 修改结果
 */
export function updateAssetLedger(data: any) {
  return http.request({ url: '/asset/ledger', method: 'put', data })
}

/**
 * 导出资产台账。
 * @param data 查询条件
 * @returns 导出的二进制文件
 */
export function exportAssetLedger(data?: any) {
  return http.request<Blob>({
    url: '/asset/ledger/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

/**
 * 获取下一条建议资产编号。
 * @returns 资产编号
 */
export function getNextAssetCode() {
  return http
    .request({
      url: '/asset/ledger/nextCode',
      method: 'get'
    })
    .then((response) => normalizeAssetCodeResponse(response))
}

/**
 * 兼容不同响应形态，统一提取资产编号字符串。
 * @param response 响应内容
 * @returns 资产编号
 */
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

/**
 * 查询资产分类树。
 * @returns 资产分类树
 */
export function getAssetCategoryTree() {
  return http.request<AssetTreeOption[]>({
    url: '/asset/ledger/categoryTree',
    method: 'get'
  })
}

/**
 * 查询部门树。
 * @returns 部门树
 */
export function getAssetDeptTree() {
  return http.request<AssetTreeOption[]>({
    url: '/asset/ledger/deptTree',
    method: 'get'
  })
}

/**
 * 远程搜索责任人。
 * @param query 搜索条件
 * @returns 责任人选项
 */
export function listAssetResponsibleUsers(query?: { keyword?: string }) {
  return http.request<AssetUserOption[]>({
    url: '/asset/ledger/responsibleUsers',
    method: 'get',
    params: query
  })
}

import request from '@/utils/http'

/**
 * 资产信息实体
 */
export interface AssetInfo {
  /** 资产编号 */
  assetNo: string
  /** 资产名称 */
  assetName: string
  /** 分类ID */
  categoryId: number
  /** 类型：1=不动产 2=固定资产 */
  assetType: string
  /** 归属部门 */
  deptId?: number
  /** 责任人 */
  userId?: number
  /** 状态：1=正常 2=领用中 3=维修中 4=盘点中 5=已报废 */
  status: string
  /** 创建时间 */
  createTime?: string
  /** 备注 */
  remark?: string
}

/**
 * 查询资产信息列表
 * @param params 查询参数
 */
export function listInfo(params?: any) {
  return request.get<AssetInfo[]>({
    url: '/asset/info/list',
    params
  })
}

/**
 * 获取资产信息详细信息
 * @param assetNo 资产编号
 */
export function getInfo(assetNo: string) {
  return request.get<AssetInfo>({
    url: `/asset/info/${assetNo}`
  })
}

/**
 * 新增资产信息
 * @param data 资产数据
 */
export function addInfo(data: AssetInfo) {
  return request.post({
    url: '/asset/info',
    data
  })
}

/**
 * 修改资产信息
 * @param data 资产数据
 */
export function updateInfo(data: AssetInfo) {
  return request.put({
    url: '/asset/info',
    data
  })
}

/**
 * 删除资产信息
 * @param assetNo 资产编号
 */
export function delInfo(assetNo: string) {
  return request.del({
    url: `/asset/info/${assetNo}`
  })
}

/**
 * 导出资产信息
 * @param params 查询参数
 */
export function exportInfo(params?: any) {
  return request.post({
    url: '/asset/info/export',
    params,
    responseType: 'blob'
  })
}

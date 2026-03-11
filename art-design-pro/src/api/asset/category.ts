import request from '@/utils/http'

/**
 * 资产分类实体
 */
export interface AssetCategory {
  /** 分类ID */
  id: number
  /** 父节点ID */
  parentId: number
  /** 分类名称 */
  name: string
  /** 分类编码 */
  code?: string
  /** 层级 */
  level?: number
  /** 创建时间 */
  createTime?: string
  /** 子节点 */
  children?: AssetCategory[]
}

/**
 * 查询资产分类列表
 * @param params 查询参数 (name, code 等)
 */
export function listCategory(params?: any) {
  return request.get<AssetCategory[]>({
    url: '/asset/category/list',
    params
  })
}

/**
 * 获取资产分类详细信息
 * @param id 分类ID
 */
export function getCategory(id: number) {
  return request.get<AssetCategory>({
    url: `/asset/category/${id}`
  })
}

/**
 * 新增资产分类
 * @param data 分类数据
 */
export function addCategory(data: Partial<AssetCategory>) {
  return request.post({
    url: '/asset/category',
    data
  })
}

/**
 * 修改资产分类
 * @param data 分类数据
 */
export function updateCategory(data: Partial<AssetCategory>) {
  return request.put({
    url: '/asset/category',
    data
  })
}

/**
 * 删除资产分类
 * @param id 分类ID
 */
export function delCategory(id: number) {
  return request.del({
    url: `/asset/category/${id}`
  })
}

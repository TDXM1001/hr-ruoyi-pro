import request from '@/utils/http'
import type { AssetDynamicAttrDefinition, AssetDynamicAttrDefinitionReq } from '@/types/asset'

/**
 * 根据分类加载动态属性定义。
 */
export function listCategoryAttrs(categoryId: number | string) {
  return request.get<AssetDynamicAttrDefinition[]>({
    url: `/asset/categoryAttr/category/${categoryId}`
  })
}

/**
 * 查询动态属性定义详情。
 */
export function getCategoryAttr(attrId: number | string) {
  return request.get<AssetDynamicAttrDefinition>({
    url: `/asset/categoryAttr/${attrId}`
  })
}

/**
 * 新增动态属性定义。
 */
export function addCategoryAttr(data: AssetDynamicAttrDefinitionReq) {
  return request.post({
    url: '/asset/categoryAttr',
    data
  })
}

/**
 * 修改动态属性定义。
 */
export function updateCategoryAttr(data: AssetDynamicAttrDefinitionReq) {
  return request.put({
    url: '/asset/categoryAttr',
    data
  })
}

/**
 * 禁用动态属性定义。
 */
export function disableCategoryAttr(attrId: number | string) {
  return request.put({
    url: `/asset/categoryAttr/disable/${attrId}`
  })
}

/**
 * 删除动态属性定义。
 */
export function delCategoryAttr(attrIds: Array<number | string>) {
  return request.del({
    url: `/asset/categoryAttr/${attrIds.join(',')}`
  })
}

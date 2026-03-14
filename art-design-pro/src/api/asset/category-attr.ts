import request from '@/utils/http'
import type { AssetDynamicAttrDefinition } from '@/types/asset'

/**
 * 根据分类加载动态属性定义。
 */
export function listCategoryAttrs(categoryId: number | string) {
  return request.get<AssetDynamicAttrDefinition[]>({
    url: `/asset/categoryAttr/category/${categoryId}`
  })
}

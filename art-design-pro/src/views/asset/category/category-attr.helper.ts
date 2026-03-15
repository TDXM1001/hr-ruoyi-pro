export interface AttrStatusTag {
  type: 'success' | 'info'
  label: string
}

const RESERVED_ATTR_CODES = ['asset_no', 'asset_name', 'original_value', 'property_cert_no']

/** 只有选中分类后，才展示右侧属性模板面板。 */
export function shouldShowAttrPanel(categoryId?: number) {
  return Boolean(categoryId)
}

/** 把后端状态值映射成页面标签。 */
export function buildAttrStatusTag(status?: string): AttrStatusTag {
  return status === '1' ? { type: 'info', label: '停用' } : { type: 'success', label: '启用' }
}

/** 判断字段编码是否命中系统保留字段。 */
export function isReservedAttrCode(attrCode?: string) {
  return RESERVED_ATTR_CODES.includes(
    String(attrCode || '')
      .trim()
      .toLowerCase()
  )
}

/** 统一保留字段提示文案，保证前后端提示语义一致。 */
export function getReservedCodeMessage(attrCode: string) {
  return `字段编码[${attrCode}]为系统保留字段，请调整后重试`
}

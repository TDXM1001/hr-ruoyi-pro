export interface AttrStatusTag {
  type: 'success' | 'info'
  label: string
}

interface CategoryAttrSubmitPayload {
  attrCode?: string
  attrType?: string
  dataType?: string
  isRequired?: string
  isUnique?: string
  isListDisplay?: string
  isQueryCondition?: string
  optionSourceType?: string
  [key: string]: unknown
}

const RESERVED_ATTR_CODES = ['asset_no', 'asset_name', 'original_value', 'property_cert_no']
/** 统一把前端语义值转换成后端约定的数据库编码。 */
const OPTION_SOURCE_TYPE_MAP: Record<string, string> = {
  manual: '1',
  dict: '2',
  remote: '3',
  '1': '1',
  '2': '2',
  '3': '3'
}

const normalizeAttrCode = (attrCode?: string) =>
  String(attrCode || '')
    .trim()
    .toLowerCase()

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
  return RESERVED_ATTR_CODES.includes(normalizeAttrCode(attrCode))
}

/** 统一保留字段提示文案，保证前后端提示语义一致。 */
export function getReservedCodeMessage(attrCode: string) {
  return `字段编码[${attrCode}]为系统保留字段，请调整后重试`
}

/** 统一分类模板提交口径，避免组件类型缺失导致后端插入失败。 */
export function buildCategoryAttrSubmitPayload<T extends CategoryAttrSubmitPayload>(payload: T) {
  const dataType = String(payload.dataType || 'text').trim() || 'text'
  const attrType = String(payload.attrType || dataType).trim() || dataType
  // 新增模板时补齐数据库非空字段，保证旧表单数据也能平滑提交。
  const optionSourceType = OPTION_SOURCE_TYPE_MAP[String(payload.optionSourceType || '1').trim()] || '1'

  return {
    ...payload,
    attrCode: normalizeAttrCode(payload.attrCode),
    dataType,
    attrType,
    isRequired: String(payload.isRequired || '0'),
    isUnique: String(payload.isUnique || '0'),
    isListDisplay: String(payload.isListDisplay || '0'),
    isQueryCondition: String(payload.isQueryCondition || '0'),
    optionSourceType
  }
}

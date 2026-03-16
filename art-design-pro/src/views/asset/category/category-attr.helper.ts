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
const OPTION_SOURCE_TYPE_MAP: Record<string, string> = {
  manual: '1',
  dict: '2',
  remote: '3',
  '1': '1',
  '2': '2',
  '3': '3'
}

/** 判断分类是否已选中，未选中时不展示动态属性面板。 */
export function shouldShowAttrPanel(categoryId?: number) {
  return Boolean(categoryId)
}

/** 列表状态标记统一映射。 */
export function buildAttrStatusTag(status?: string): AttrStatusTag {
  return status === '1' ? { type: 'info', label: '停用' } : { type: 'success', label: '启用' }
}

/** 将属性编码统一收口为后端约定的 snake_case 形式。 */
export function normalizeAttrCode(attrCode?: string) {
  return String(attrCode || '')
    .trim()
    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toLowerCase()
}

/** 判断是否命中系统保留字段。 */
export function isReservedAttrCode(attrCode?: string) {
  return RESERVED_ATTR_CODES.includes(normalizeAttrCode(attrCode))
}

/** 返回系统保留字段提示文案。 */
export function getReservedCodeMessage(attrCode: string) {
  return `字段编码[${attrCode}]为系统保留字段，不允许使用`
}

/** 在提交前拦截系统保留字段，避免无效请求进入后端。 */
export function validateReservedAttrCode(attrCode?: string) {
  const normalizedAttrCode = normalizeAttrCode(attrCode)
  if (isReservedAttrCode(normalizedAttrCode)) {
    throw new Error(getReservedCodeMessage(normalizedAttrCode))
  }
  return normalizedAttrCode
}

/** 构建提交给后端的动态属性载荷，统一补齐默认值和枚举口径。 */
export function buildCategoryAttrSubmitPayload<T extends CategoryAttrSubmitPayload>(payload: T) {
  const dataType = String(payload.dataType || 'text').trim() || 'text'
  const attrType = String(payload.attrType || dataType).trim() || dataType
  const optionSourceType =
    OPTION_SOURCE_TYPE_MAP[String(payload.optionSourceType || '1').trim()] || '1'

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

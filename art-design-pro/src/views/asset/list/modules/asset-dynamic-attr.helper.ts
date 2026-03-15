import type { AssetDynamicAttrDefinition, AssetDynamicAttrValue } from '@/types/asset'

/** 保留字段编码，这些字段已经由基础表单承载，不能再动态重复配置。 */
const RESERVED_ATTR_CODES = ['asset_no', 'asset_name', 'original_value', 'property_cert_no']

/** 找出与系统保留字段冲突的动态属性编码。 */
export function findReservedAttrCodes(
  definitions: Array<Pick<AssetDynamicAttrDefinition, 'attrCode'>>
) {
  return definitions
    .map((item) => item.attrCode)
    .filter((code) => RESERVED_ATTR_CODES.includes(code))
}

/** 把后端动态属性值回填成前端表单记录。 */
export function toDynamicAttrFormRecord(items: AssetDynamicAttrValue[] = []) {
  return items.reduce<Record<string, unknown>>((acc, item) => {
    acc[item.attrCode] =
      item.attrValueText ?? item.attrValueNumber ?? item.attrValueDate ?? item.attrValueJson ?? ''
    return acc
  }, {})
}

/** 按字段类型构建提交给后端的动态属性值数组。 */
export function buildDynamicAttrPayload(
  definitions: Array<
    Pick<AssetDynamicAttrDefinition, 'attrId' | 'categoryId' | 'attrCode' | 'dataType'>
  >,
  formRecord: Record<string, unknown>
) {
  return definitions.map((item) => {
    const value = formRecord[item.attrCode]
    const payload: AssetDynamicAttrValue = {
      attrId: item.attrId,
      categoryId: item.categoryId,
      attrCode: item.attrCode
    }

    if (item.dataType === 'number') {
      payload.attrValueNumber =
        value === '' || value === undefined || value === null ? undefined : Number(value)
      return payload
    }

    if (item.dataType === 'date') {
      payload.attrValueDate = String(value || '')
      return payload
    }

    if (item.dataType === 'json') {
      payload.attrValueJson = String(value || '')
      return payload
    }

    payload.attrValueText = String(value || '')
    return payload
  })
}

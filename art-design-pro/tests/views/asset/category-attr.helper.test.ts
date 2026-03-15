import { describe, expect, it } from 'vitest'
import {
  buildCategoryAttrSubmitPayload,
  buildAttrStatusTag,
  getReservedCodeMessage,
  shouldShowAttrPanel
} from '../../../src/views/asset/category/category-attr.helper'

describe('category attr helper', () => {
  it('maps attr status tag', () => {
    expect(buildAttrStatusTag('0')).toEqual({ type: 'success', label: '启用' })
    expect(buildAttrStatusTag('1')).toEqual({ type: 'info', label: '停用' })
  })

  it('builds reserved code message', () => {
    expect(getReservedCodeMessage('asset_no')).toContain('系统保留字段')
  })

  it('shows panel only when category is selected', () => {
    expect(shouldShowAttrPanel(undefined)).toBe(false)
    expect(shouldShowAttrPanel(10)).toBe(true)
  })

  it('backfills attr type from data type when building submit payload', () => {
    expect(
      buildCategoryAttrSubmitPayload({
        categoryId: 10,
        attrCode: ' Manufacturer ',
        attrName: '厂商',
        dataType: 'text',
        optionSourceType: 'manual'
      })
    ).toMatchObject({
      categoryId: 10,
      attrCode: 'manufacturer',
      attrName: '厂商',
      attrType: 'text',
      dataType: 'text',
      isRequired: '0',
      isUnique: '0',
      isListDisplay: '0',
      isQueryCondition: '0',
      optionSourceType: '1'
    })
  })

  it('keeps explicit attr type when building submit payload', () => {
    expect(
      buildCategoryAttrSubmitPayload({
        categoryId: 10,
        attrCode: 'status',
        attrName: '状态',
        attrType: 'radio',
        dataType: 'select',
        isUnique: '1',
        isListDisplay: '1',
        isQueryCondition: '1',
        optionSourceType: '2'
      })
    ).toMatchObject({
      attrCode: 'status',
      attrType: 'radio',
      dataType: 'select',
      isUnique: '1',
      isListDisplay: '1',
      isQueryCondition: '1',
      optionSourceType: '2'
    })
  })
})

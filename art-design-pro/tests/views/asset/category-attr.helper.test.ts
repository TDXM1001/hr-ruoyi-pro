import { describe, expect, it } from 'vitest'
import {
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
})

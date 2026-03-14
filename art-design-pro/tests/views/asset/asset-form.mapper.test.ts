import { describe, expect, it } from 'vitest'

import {
  buildAggregatePayload,
  createEmptyDrawerState,
  hydrateDrawerState
} from '../../../src/views/asset/list/modules/asset-form.mapper'

describe('asset form mapper', () => {
  it('creates empty drawer state', () => {
    const state = createEmptyDrawerState()

    expect(state.basicForm.assetNo).toBe('')
    expect(state.financeForm.currencyCode).toBe('CNY')
    expect(state.realEstateForm.propertyCertNo).toBe('')
    expect(state.attachments).toEqual([])
  })

  it('hydrates drawer state from aggregate detail', () => {
    const state = hydrateDrawerState({
      basicInfo: { assetId: 1, assetNo: 'FA-001', assetName: '房产', assetType: '2', assetStatus: '1' },
      financeInfo: {
        bookType: '1',
        currencyCode: 'CNY',
        originalValue: 1000000,
        salvageRate: 0.03,
        depreciationMethod: '1',
        usefulLifeMonth: 360,
        depreciationStartDate: '2026-03-01'
      },
      realEstateInfo: { propertyCertNo: '沪(2026)001号', addressFull: '测试地址' },
      dynamicAttrs: [],
      attachments: [],
      depreciationLogs: []
    })

    expect(state.basicForm.assetId).toBe(1)
    expect(state.realEstateForm.propertyCertNo).toBe('沪(2026)001号')
  })

  it('keeps assetId only in edit payload', () => {
    const state = createEmptyDrawerState()
    state.basicForm.assetId = 1
    state.basicForm.assetNo = 'FA-001'
    state.basicForm.assetName = '电脑'
    state.basicForm.assetType = '1'
    state.basicForm.assetStatus = '1'

    const addPayload = buildAggregatePayload(state, 'add')
    const editPayload = buildAggregatePayload(state, 'edit')

    expect(addPayload.basicInfo.assetId).toBeUndefined()
    expect(editPayload.basicInfo.assetId).toBe(1)
  })
})

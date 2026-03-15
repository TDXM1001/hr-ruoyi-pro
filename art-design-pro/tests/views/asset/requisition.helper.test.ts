import { describe, expect, it } from 'vitest'

import {
  buildApplyRequisitionReq,
  canReturnAsset,
  mapRequisitionStatusToWorkflow
} from '../../../src/views/asset/requisition/requisition.helper'

describe('requisition helper', () => {
  it('builds apply payload with assetId', () => {
    expect(
      buildApplyRequisitionReq(
        { assetId: 1, assetNo: 'FA-001', assetName: '电脑', assetStatus: '1' },
        '领用测试'
      )
    ).toEqual({
      assetId: 1,
      assetNo: 'FA-001',
      reason: '领用测试'
    })
  })

  it('maps requisition status to workflow tag', () => {
    expect(mapRequisitionStatusToWorkflow(0)).toBe('IN_PROGRESS')
    expect(mapRequisitionStatusToWorkflow(1)).toBe('COMPLETED')
    expect(mapRequisitionStatusToWorkflow(2)).toBe('REJECTED')
    expect(mapRequisitionStatusToWorkflow(3)).toBe('COMPLETED')
  })

  it('shows return button only for approved rows', () => {
    expect(canReturnAsset({ status: 1 })).toBe(true)
    expect(canReturnAsset({ status: 0 })).toBe(false)
  })
})

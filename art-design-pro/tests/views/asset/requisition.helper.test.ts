import { describe, expect, it } from 'vitest'

import {
  buildApplyRequisitionReq,
  buildFixedAssetBusinessPayload,
  canReturnAsset,
  formatFixedAssetBusinessStatus,
  mapRequisitionStatusToWorkflow,
  resolveFixedAssetWorkflowStatus
} from '../../../src/views/asset/requisition/requisition.helper'

describe('requisition helper', () => {
  it('builds apply payload with assetId', () => {
    expect(
      buildApplyRequisitionReq(
        { assetId: 1, assetNo: 'FA-001', assetName: '电脑', assetStatus: '1' },
        '领用办公电脑'
      )
    ).toEqual({
      assetId: 1,
      assetNo: 'FA-001',
      reason: '领用办公电脑'
    })
  })

  it('requires assetId when building fixed asset business payload', () => {
    expect(() => buildFixedAssetBusinessPayload({ assetNo: 'FA-001' }, '维修申请')).toThrow(
      '资产主键缺失'
    )
  })

  it('maps requisition status to workflow tag', () => {
    expect(mapRequisitionStatusToWorkflow(0)).toBe('pending')
    expect(mapRequisitionStatusToWorkflow(1)).toBe('approved')
    expect(mapRequisitionStatusToWorkflow(2)).toBe('rejected')
    expect(mapRequisitionStatusToWorkflow(3)).toBe('completed')
  })

  it('prefers explicit wfStatus over derived status mapping', () => {
    expect(resolveFixedAssetWorkflowStatus({ status: 1, wfStatus: 'completed' })).toBe('completed')
    expect(resolveFixedAssetWorkflowStatus({ status: 1 })).toBe('approved')
  })

  it('formats fixed asset business status label', () => {
    expect(formatFixedAssetBusinessStatus(0)).toBe('待审批')
    expect(formatFixedAssetBusinessStatus(1)).toBe('已审批')
    expect(formatFixedAssetBusinessStatus(2)).toBe('已驳回')
    expect(formatFixedAssetBusinessStatus(3)).toBe('已完成')
  })

  it('shows return button only for approved rows', () => {
    expect(canReturnAsset({ status: 1 })).toBe(true)
    expect(canReturnAsset({ status: 0 })).toBe(false)
  })
})

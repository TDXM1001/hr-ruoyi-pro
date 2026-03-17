import { describe, expect, it } from 'vitest'

import {
  buildOwnershipChangePayload,
  buildRealEstateDisposalPayload,
  buildStatusChangePayload,
  buildUsageChangePayload,
  mapRealEstateStatusToWorkflow,
  parseAssetRouteQuery,
  resolveRealEstateWorkflowStatus,
  shouldOpenCreateDialog
} from '../../../src/views/asset/real-estate/real-estate-lifecycle.helper'

describe('real estate ledger helper', () => {
  it('parses asset route query into page context', () => {
    expect(
      parseAssetRouteQuery({
        assetId: '1001',
        assetNo: 'RE-001',
        assetName: '办公楼 A',
        assetStatus: '1'
      })
    ).toMatchObject({
      assetId: 1001,
      assetNo: 'RE-001',
      assetName: '办公楼 A',
      assetStatus: '1'
    })
  })

  it('keeps asset context when route has assetNo only', () => {
    expect(
      parseAssetRouteQuery({
        assetId: 'not-a-number',
        assetNo: 'RE-002'
      })
    ).toMatchObject({
      assetNo: 'RE-002'
    })
  })

  it('maps approval and direct-complete statuses to workflow dictionary', () => {
    expect(mapRealEstateStatusToWorkflow('pending')).toBe('IN_PROGRESS')
    expect(mapRealEstateStatusToWorkflow('approved')).toBe('COMPLETED')
    expect(mapRealEstateStatusToWorkflow('completed')).toBe('COMPLETED')
    expect(mapRealEstateStatusToWorkflow('rejected')).toBe('REJECTED')
  })

  it('prefers wfStatus when resolving workflow display', () => {
    expect(
      resolveRealEstateWorkflowStatus({
        status: 'pending',
        wfStatus: 'completed'
      })
    ).toBe('COMPLETED')
  })

  it('keeps latest timeline snapshot in route context for real estate pages', () => {
    expect(
      parseAssetRouteQuery({
        assetId: '1001',
        assetNo: 'RE-003',
        latestActionType: 'REAL_ESTATE_OWNERSHIP_CHANGE',
        latestDocStatus: 'approved',
        latestActionTime: '2026-03-15 10:30:00'
      })
    ).toMatchObject({
      assetId: 1001,
      assetNo: 'RE-003',
      latestActionType: 'REAL_ESTATE_OWNERSHIP_CHANGE',
      latestDocStatus: 'approved',
      latestActionTime: '2026-03-15 10:30:00'
    })
  })

  it('requires route assetId before auto-opening create dialog', () => {
    expect(shouldOpenCreateDialog({ openCreate: '1' })).toBe(false)
    expect(shouldOpenCreateDialog({ openCreate: '0' })).toBe(false)
  })

  it('blocks auto-open when the same real estate action is still pending', () => {
    expect(
      shouldOpenCreateDialog({
        openCreate: '1',
        actionKey: 'realEstateOwnership',
        assetStatus: '1',
        latestActionType: 'REAL_ESTATE_OWNERSHIP_CHANGE',
        latestDocStatus: 'pending'
      })
    ).toBe(false)
  })

  it('blocks auto-open for disposed real estate assets', () => {
    expect(
      shouldOpenCreateDialog({
        openCreate: '1',
        actionKey: 'realEstateDisposal',
        assetId: '1001',
        assetNo: 'RE-004',
        assetStatus: '6'
      })
    ).toBe(false)
  })

  it('builds unified real estate business payloads from route asset context', () => {
    const context = parseAssetRouteQuery({
      assetId: '1001',
      assetNo: 'RE-1001',
      assetName: '不动产 A',
      assetStatus: '1'
    })

    expect(
      buildOwnershipChangePayload(context, {
        assetNo: '',
        targetRightsHolder: '张三',
        targetPropertyCertNo: 'CERT-NEW',
        targetRegistrationDate: '2026-03-16',
        reason: '权属变更'
      })
    ).toMatchObject({
      assetId: 1001,
      assetNo: 'RE-1001',
      targetRightsHolder: '张三'
    })

    expect(
      buildUsageChangePayload(context, {
        assetNo: '',
        targetLandUse: '住宅',
        targetBuildingUse: '商业',
        reason: '用途调整'
      })
    ).toMatchObject({
      assetId: 1001,
      assetNo: 'RE-1001',
      targetLandUse: '住宅'
    })

    expect(
      buildStatusChangePayload(context, {
        assetNo: '',
        targetAssetStatus: '7',
        reason: '状态变更'
      })
    ).toMatchObject({
      assetId: 1001,
      assetNo: 'RE-1001',
      targetAssetStatus: '7'
    })

    expect(
      buildRealEstateDisposalPayload(context, {
        assetNo: '',
        disposalType: 'cancel',
        targetAssetStatus: '',
        reason: '注销处置'
      })
    ).toMatchObject({
      assetId: 1001,
      assetNo: 'RE-1001',
      targetAssetStatus: '6'
    })
  })

  it('rejects manual payload build when route assetId is missing', () => {
    const context = parseAssetRouteQuery({
      assetNo: 'RE-1002'
    })

    expect(() =>
      buildOwnershipChangePayload(context, {
        assetNo: 'RE-1002',
        targetRightsHolder: '李四',
        targetPropertyCertNo: '',
        targetRegistrationDate: '',
        reason: '权属变更'
      })
    ).toThrow('请从资产台账选择不动产后再发起业务单据')
  })
})

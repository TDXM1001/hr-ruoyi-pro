import { describe, expect, it } from 'vitest'

import {
  mapRealEstateStatusToWorkflow,
  parseAssetRouteQuery,
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

  it('opens create dialog only when query explicitly asks for it', () => {
    expect(shouldOpenCreateDialog({ openCreate: '1' })).toBe(true)
    expect(shouldOpenCreateDialog({ openCreate: '0' })).toBe(false)
  })
})

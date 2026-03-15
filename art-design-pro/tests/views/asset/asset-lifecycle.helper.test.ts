import { describe, expect, it } from 'vitest'

import { buildLifecycleActions } from '../../../src/views/asset/list/asset-lifecycle.helper'

describe('asset lifecycle helper', () => {
  it('returns fixed asset actions', () => {
    expect(
      buildLifecycleActions({ assetType: '1', assetStatus: '1' }).map((item) => item.key)
    ).toEqual(expect.arrayContaining(['change', 'delete', 'requisition', 'repair', 'disposal']))
  })

  it('uses real actions for fixed asset repair and disposal entries', () => {
    const actions = buildLifecycleActions({ assetType: '1', assetStatus: '1' })

    expect(actions.find((item) => item.key === 'repair')?.mode).toBe('action')
    expect(actions.find((item) => item.key === 'disposal')?.mode).toBe('action')
  })

  it('filters out fixed-asset-only actions for real estate', () => {
    expect(
      buildLifecycleActions({ assetType: '2', assetStatus: '1' }).map((item) => item.key)
    ).not.toEqual(expect.arrayContaining(['requisition', 'repair']))
  })

  it('keeps common actions for real estate assets', () => {
    expect(
      buildLifecycleActions({ assetType: '2', assetStatus: '1' }).map((item) => item.key)
    ).toEqual(expect.arrayContaining(['change', 'delete', 'realEstateChange', 'disposal']))
  })
})

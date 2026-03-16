import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

import { buildLifecycleActions } from '../../../src/views/asset/list/asset-lifecycle.helper'

describe('asset lifecycle helper', () => {
  const assetTypesSource = readFileSync(resolve(process.cwd(), 'src/types/asset.ts'), 'utf8')

  it('locks shared asset status semantics in the type definition file', () => {
    expect(assetTypesSource).toContain('1=固定资产，2=不动产')
    expect(assetTypesSource).toContain('1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置')
  })

  it('returns fixed asset actions for active assets', () => {
    expect(
      buildLifecycleActions({ assetType: '1', assetStatus: '1' }).map((item) => item.key)
    ).toEqual(expect.arrayContaining(['change', 'delete', 'requisition', 'repair', 'scrap', 'dispose']))
  })

  it('keeps lifecycle actions for idle fixed assets', () => {
    expect(
      buildLifecycleActions({ assetType: '1', assetStatus: '7' }).map((item) => item.key)
    ).toEqual(expect.arrayContaining(['requisition', 'repair', 'scrap', 'dispose']))
  })

  it('uses split actions for active fixed asset scrap and disposal entries', () => {
    const actions = buildLifecycleActions({ assetType: '1', assetStatus: '1' })

    expect(actions.find((item) => item.key === 'repair')?.mode).toBe('action')
    expect(actions.find((item) => item.key === 'scrap')?.label).toBe('报废')
    expect(actions.find((item) => item.key === 'dispose')?.label).toBe('处置')
  })

  it('hides fixed asset lifecycle actions after the asset is scrapped', () => {
    expect(
      buildLifecycleActions({ assetType: '1', assetStatus: '5' }).map((item) => item.key)
    ).not.toEqual(expect.arrayContaining(['requisition', 'repair', 'scrap', 'dispose']))
  })

  it('hides fixed asset lifecycle actions after the asset is disposed', () => {
    expect(
      buildLifecycleActions({ assetType: '1', assetStatus: '6' }).map((item) => item.key)
    ).not.toEqual(expect.arrayContaining(['requisition', 'repair', 'scrap', 'dispose']))
  })

  it('filters out fixed-asset-only actions for real estate', () => {
    expect(
      buildLifecycleActions({ assetType: '2', assetStatus: '1' }).map((item) => item.key)
    ).not.toEqual(expect.arrayContaining(['requisition', 'repair']))
  })

  it('keeps common actions for real estate assets', () => {
    expect(
      buildLifecycleActions({ assetType: '2', assetStatus: '1' }).map((item) => item.key)
    ).toEqual(
      expect.arrayContaining([
        'change',
        'delete',
        'realEstateOwnership',
        'realEstateUsage',
        'realEstateStatus',
        'realEstateDisposal'
      ])
    )
  })

  it('exposes real estate actions as formal action entries', () => {
    const actions = buildLifecycleActions({ assetType: '2', assetStatus: '1' })

    expect(actions.find((item) => item.key === 'realEstateOwnership')?.mode).toBe('action')
    expect(actions.find((item) => item.key === 'realEstateUsage')?.mode).toBe('action')
    expect(actions.find((item) => item.key === 'realEstateStatus')?.mode).toBe('action')
    expect(actions.find((item) => item.key === 'realEstateDisposal')?.mode).toBe('action')
  })
})

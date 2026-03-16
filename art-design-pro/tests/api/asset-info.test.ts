import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { beforeEach, describe, expect, expectTypeOf, it, vi } from 'vitest'

const { http } = vi.hoisted(() => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
    request: vi.fn()
  }
}))

vi.mock('@/utils/http', () => ({
  default: http
}))

import type { AssetAggregateReq, AssetBusinessOrderBase, AssetRef } from '../../src/types/asset'
import * as assetInfoApi from '../../src/api/asset/info'

describe('Asset Info API', () => {
  const assetTypesSource = readFileSync(resolve(process.cwd(), 'src/types/asset.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts aggregate payload for add api', () => {
    expectTypeOf<Parameters<typeof assetInfoApi.addInfo>[0]>().toEqualTypeOf<AssetAggregateReq>()
  })

  it('uses assetId for detail and archive', async () => {
    await assetInfoApi.getInfo(101)
    await (assetInfoApi as any).archiveInfo(101)

    expectTypeOf<Parameters<typeof assetInfoApi.getInfo>[0]>().toEqualTypeOf<number | string>()
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/info/101' })
    expect(http.del).toHaveBeenCalledWith({ url: '/asset/info/101' })
  })

  it('locks aggregate detail timeline contract in shared asset types', () => {
    expect(assetTypesSource).toContain('export interface AssetTimelineItem')
    expect(assetTypesSource).toContain('timeline: AssetTimelineItem[]')
  })

  it('locks shared asset ref and business order base contracts', () => {
    expectTypeOf<AssetRef>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      assetName: string
      assetType: string
      assetStatus: string
    }>()

    expectTypeOf<AssetBusinessOrderBase>().toEqualTypeOf<{
      bizNo?: string
      bizType?: string
      assetId: number
      assetNo: string
      assetName?: string
      assetStatus?: string
      status: string | number
      wfStatus?: string
      archiveStatus?: string
      reason?: string
      createTime?: string
    }>()

    expect(assetTypesSource).toContain('archiveStatus')
  })
})

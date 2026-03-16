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

import type { AssetAggregateReq } from '../../src/types/asset'
import * as assetInfoApi from '../../src/api/asset/info'

describe('Asset Info API', () => {
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
})

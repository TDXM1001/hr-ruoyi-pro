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
import { addInfo, delInfo, getInfo } from '../../src/api/asset/info'

describe('Asset Info API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts aggregate payload for add api', () => {
    expectTypeOf<Parameters<typeof addInfo>[0]>().toEqualTypeOf<AssetAggregateReq>()
  })

  it('uses assetId for detail and delete', async () => {
    await getInfo(101)
    await delInfo(101)

    expectTypeOf<Parameters<typeof getInfo>[0]>().toEqualTypeOf<number | string>()
    expectTypeOf<Parameters<typeof delInfo>[0]>().toEqualTypeOf<number | string>()
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/info/101' })
    expect(http.del).toHaveBeenCalledWith({ url: '/asset/info/101' })
  })
})

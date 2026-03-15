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

import {
  createRealEstateDisposal,
  getRealEstateDisposalDetail,
  listRealEstateDisposal
} from '../../src/api/asset/real-estate-disposal'

describe('Real Estate Disposal API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and disposal fields in payload type', () => {
    expectTypeOf<Parameters<typeof createRealEstateDisposal>[0]>().toEqualTypeOf<{
      assetId?: number
      assetNo?: string
      disposalType?: string
      targetAssetStatus?: string
      reason: string
    }>()
  })

  it('queries disposal list from real estate disposal endpoint', async () => {
    const params = { disposalNo: 'RED-20260315-001', assetNo: 'RE-2026-0001', status: 'pending' }

    await listRealEstateDisposal(params)

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/disposal/list',
      params
    })
  })

  it('queries disposal detail by disposalNo', async () => {
    await getRealEstateDisposalDetail('RED-20260315-001')

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/disposal/RED-20260315-001'
    })
  })

  it('posts disposal payload', async () => {
    const data = {
      assetId: 2004,
      assetNo: 'RE-2026-0004',
      disposalType: 'cancel',
      targetAssetStatus: '6',
      reason: '产权注销'
    }

    await createRealEstateDisposal(data)

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/real-estate/disposal',
      data
    })
  })
})

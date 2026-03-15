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

import { applyDisposal, getDisposal, listDisposal } from '../../src/api/asset/disposal'

describe('Asset Disposal API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and assetNo in disposal payload type', () => {
    expectTypeOf<Parameters<typeof applyDisposal>[0]>().toEqualTypeOf<{
      assetId?: number
      assetNo: string
      reason: string
    }>()
  })

  it('queries disposal list from disposal endpoint', async () => {
    const params = { disposalNo: 'DIS-20260315-001', assetNo: 'FA-2026-0003', status: 1 }

    await listDisposal(params)

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/disposal/list',
      params
    })
  })

  it('queries disposal detail by disposalNo', async () => {
    await getDisposal('DIS-20260315-001')

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/disposal/DIS-20260315-001'
    })
  })

  it('posts disposal apply payload', async () => {
    await applyDisposal({ assetId: 103, assetNo: 'FA-2026-0003', reason: '报废测试' })

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/disposal',
      data: { assetId: 103, assetNo: 'FA-2026-0003', reason: '报废测试' }
    })
  })
})

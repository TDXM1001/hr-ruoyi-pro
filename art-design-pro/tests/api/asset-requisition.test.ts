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

import { applyRequisition } from '../../src/api/asset/requisition'

describe('Asset Requisition API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and assetNo in apply payload type', () => {
    expectTypeOf<Parameters<typeof applyRequisition>[0]>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      reason: string
    }>()
  })

  it('posts requisition with assetId and assetNo', async () => {
    await applyRequisition({ assetId: 101, assetNo: 'FA-2026-0001', reason: '领用测试' })

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/requisition',
      data: { assetId: 101, assetNo: 'FA-2026-0001', reason: '领用测试' }
    })
  })
})

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

import {
  applyDisposal,
  getDisposal,
  listDisposal,
  type AssetDisposalItem
} from '../../src/api/asset/disposal'

describe('Asset Disposal API', () => {
  const disposalApiSource = readFileSync(resolve(process.cwd(), 'src/api/asset/disposal.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and assetNo in disposal payload type', () => {
    expectTypeOf<Parameters<typeof applyDisposal>[0]>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      disposalType: 'scrap' | 'sell' | 'transfer' | 'donate'
      reason: string
    }>()
  })

  it('locks disposal row contract with assetId and wfStatus', () => {
    expectTypeOf<AssetDisposalItem>()
    expect(disposalApiSource).toContain('assetId: number')
    expect(disposalApiSource).toContain('assetNo: string')
    expect(disposalApiSource).toContain('wfStatus?: string')
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
    await applyDisposal({
      assetId: 103,
      assetNo: 'FA-2026-0003',
      disposalType: 'sell',
      reason: '处置测试'
    })

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/disposal',
      data: {
        assetId: 103,
        assetNo: 'FA-2026-0003',
        disposalType: 'sell',
        reason: '处置测试'
      }
    })
  })
})

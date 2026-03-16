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
  applyRequisition,
  returnAsset,
  type AssetRequisitionItem
} from '../../src/api/asset/requisition'

describe('Asset Requisition API', () => {
  const requisitionApiSource = readFileSync(resolve(process.cwd(), 'src/api/asset/requisition.ts'), 'utf8')

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

  it('locks requisition row contract with assetId and wfStatus', () => {
    expectTypeOf<AssetRequisitionItem>()
    expect(requisitionApiSource).toContain('assetId: number')
    expect(requisitionApiSource).toContain('assetNo: string')
    expect(requisitionApiSource).toContain('wfStatus?: string')
  })

  it('posts return request to requisition return endpoint', async () => {
    await returnAsset('REQ-20260315-001')

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/requisition/return/REQ-20260315-001'
    })
  })
})

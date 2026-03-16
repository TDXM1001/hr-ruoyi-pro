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
  createUsageChange,
  getUsageChangeDetail,
  listUsageChange,
  type UsageChangeItem
} from '../../src/api/asset/real-estate-usage'

describe('Real Estate Usage API', () => {
  const usageApiSource = readFileSync(resolve(process.cwd(), 'src/api/asset/real-estate-usage.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and usage target fields in payload type', () => {
    expectTypeOf<Parameters<typeof createUsageChange>[0]>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      targetLandUse?: string
      targetBuildingUse?: string
      reason: string
    }>()
  })

  it('locks usage change row contract with assetId and wfStatus', () => {
    expectTypeOf<UsageChangeItem>()
    expect(usageApiSource).toContain('assetId: number')
    expect(usageApiSource).toContain('assetNo: string')
    expect(usageApiSource).toContain('wfStatus?: string')
  })

  it('queries usage list from real estate usage endpoint', async () => {
    const params = { usageChangeNo: 'REU-20260315-001', assetNo: 'RE-2026-0001', status: 'completed' }

    await listUsageChange(params)

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/usage/list',
      params
    })
  })

  it('queries usage detail by usageChangeNo', async () => {
    await getUsageChangeDetail('REU-20260315-001')

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/usage/REU-20260315-001'
    })
  })

  it('posts usage change payload', async () => {
    const data = {
      assetId: 2002,
      assetNo: 'RE-2026-0002',
      targetLandUse: '居住',
      targetBuildingUse: '住宅',
      reason: '用途调整'
    }

    await createUsageChange(data)

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/real-estate/usage',
      data
    })
  })
})

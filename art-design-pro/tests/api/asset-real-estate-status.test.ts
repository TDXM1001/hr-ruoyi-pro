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
  createStatusChange,
  getStatusChangeDetail,
  listStatusChange,
  type StatusChangeItem
} from '../../src/api/asset/real-estate-status'

describe('Real Estate Status API', () => {
  const statusApiSource = readFileSync(resolve(process.cwd(), 'src/api/asset/real-estate-status.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and target status in payload type', () => {
    expectTypeOf<Parameters<typeof createStatusChange>[0]>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      targetAssetStatus: string
      reason: string
    }>()
  })

  it('locks status change row contract with assetId and wfStatus', () => {
    expectTypeOf<StatusChangeItem>()
    expect(statusApiSource).toContain('assetId: number')
    expect(statusApiSource).toContain('assetNo: string')
    expect(statusApiSource).toContain('wfStatus?: string')
  })

  it('queries status list from real estate status endpoint', async () => {
    const params = { statusChangeNo: 'RES-20260315-001', assetNo: 'RE-2026-0001', status: 'completed' }

    await listStatusChange(params)

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/status/list',
      params
    })
  })

  it('queries status detail by statusChangeNo', async () => {
    await getStatusChangeDetail('RES-20260315-001')

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/status/RES-20260315-001'
    })
  })

  it('posts status change payload', async () => {
    const data = {
      assetId: 2003,
      assetNo: 'RE-2026-0003',
      targetAssetStatus: '7',
      reason: '转为闲置'
    }

    await createStatusChange(data)

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/real-estate/status',
      data
    })
  })
})

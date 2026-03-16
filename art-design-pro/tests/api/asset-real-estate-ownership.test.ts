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
  createOwnershipChange,
  getOwnershipChangeDetail,
  listOwnershipChange,
  type OwnershipChangeItem
} from '../../src/api/asset/real-estate-ownership'

describe('Real Estate Ownership API', () => {
  const ownershipApiSource = readFileSync(
    resolve(process.cwd(), 'src/api/asset/real-estate-ownership.ts'),
    'utf8'
  )

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and ownership target fields in payload type', () => {
    expectTypeOf<Parameters<typeof createOwnershipChange>[0]>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      targetRightsHolder: string
      targetPropertyCertNo?: string
      targetRegistrationDate?: string
      reason: string
    }>()
  })

  it('locks ownership change row contract with assetId and wfStatus', () => {
    expectTypeOf<OwnershipChangeItem>()
    expect(ownershipApiSource).toContain('assetId: number')
    expect(ownershipApiSource).toContain('assetNo: string')
    expect(ownershipApiSource).toContain('wfStatus?: string')
  })

  it('queries ownership list from real estate ownership endpoint', async () => {
    const params = { ownershipChangeNo: 'OWN-20260315-001', assetNo: 'RE-2026-0001', status: 'pending' }

    await listOwnershipChange(params)

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/ownership/list',
      params
    })
  })

  it('queries ownership detail by ownershipChangeNo', async () => {
    await getOwnershipChangeDetail('OWN-20260315-001')

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/real-estate/ownership/OWN-20260315-001'
    })
  })

  it('posts ownership change payload', async () => {
    const data = {
      assetId: 2001,
      assetNo: 'RE-2026-0001',
      targetRightsHolder: '张三',
      targetPropertyCertNo: '沪(2026)001号',
      targetRegistrationDate: '2026-03-15',
      reason: '产权过户'
    }

    await createOwnershipChange(data)

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/real-estate/ownership',
      data
    })
  })
})

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
  applyMaintenance,
  completeMaintenance,
  getMaintenance,
  listMaintenance,
  type AssetMaintenanceItem
} from '../../src/api/asset/maintenance'

describe('Asset Maintenance API', () => {
  const maintenanceApiSource = readFileSync(resolve(process.cwd(), 'src/api/asset/maintenance.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts assetId and assetNo in maintenance payload type', () => {
    expectTypeOf<Parameters<typeof applyMaintenance>[0]>().toEqualTypeOf<{
      assetId: number
      assetNo: string
      reason: string
    }>()
  })

  it('locks maintenance row contract with assetId and wfStatus', () => {
    expectTypeOf<AssetMaintenanceItem>()
    expect(maintenanceApiSource).toContain('assetId: number')
    expect(maintenanceApiSource).toContain('assetNo: string')
    expect(maintenanceApiSource).toContain('wfStatus?: string')
  })

  it('queries maintenance list from maintenance endpoint', async () => {
    const params = { maintenanceNo: 'MNT-20260315-001', assetNo: 'FA-2026-0002', status: 1 }

    await listMaintenance(params)

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/maintenance/list',
      params
    })
  })

  it('queries maintenance detail by maintenanceNo', async () => {
    await getMaintenance('MNT-20260315-001')

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/maintenance/MNT-20260315-001'
    })
  })

  it('posts maintenance apply payload', async () => {
    await applyMaintenance({ assetId: 102, assetNo: 'FA-2026-0002', reason: '维修测试' })

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/maintenance',
      data: { assetId: 102, assetNo: 'FA-2026-0002', reason: '维修测试' }
    })
  })

  it('posts maintenance completion request', async () => {
    await completeMaintenance('MNT-20260315-001')

    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/maintenance/complete/MNT-20260315-001'
    })
  })
})

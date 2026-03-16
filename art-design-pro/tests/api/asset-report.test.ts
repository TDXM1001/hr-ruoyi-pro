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

import * as assetReportApi from '../../src/api/asset/report'
import type { AssetWarningItem } from '../../src/api/asset/report'

describe('Asset Report API', () => {
  const reportApiSource = readFileSync(resolve(process.cwd(), 'src/api/asset/report.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('queries report summary from report endpoint', async () => {
    await assetReportApi.getAssetReportSummary()

    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/report/summary'
    })
  })

  it('queries report warnings with land term filter', async () => {
    await assetReportApi.getAssetWarnings({ landTermWithinDays: 45 })

    expectTypeOf<Parameters<typeof assetReportApi.getAssetWarnings>[0]>().toEqualTypeOf<
      | {
          landTermWithinDays?: number
        }
      | undefined
    >()
    expect(http.get).toHaveBeenCalledWith({
      url: '/asset/report/warnings',
      params: { landTermWithinDays: 45 }
    })
  })

  it('locks warning row contract with unified status fields', () => {
    expectTypeOf<AssetWarningItem>()
    expect(reportApiSource).toContain('assetStatus?: string')
    expect(reportApiSource).toContain('status?: string')
    expect(reportApiSource).toContain('wfStatus?: string')
    expect(reportApiSource).toContain('archiveStatus?: string')
  })
})

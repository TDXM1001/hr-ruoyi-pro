import { beforeEach, describe, expect, it, vi } from 'vitest'

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

import { listDepreciationLogs, recalculateFinance } from '../../src/api/asset/finance'

describe('Asset Finance API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uses finance endpoints by assetId', async () => {
    await recalculateFinance(101)
    await listDepreciationLogs(101)

    expect(http.post).toHaveBeenCalledWith({ url: '/asset/finance/101/recalculate' })
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/finance/101/depreciation-logs' })
  })
})

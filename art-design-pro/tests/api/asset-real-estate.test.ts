import { vi, describe, it, expect } from 'vitest'

vi.mock('@/utils/http', () => {
  return {
    default: {
      request: vi.fn()
    }
  }
})

import http from '@/utils/http'
import { getRealEstateList, getRealEstateDetail } from '../../src/api/asset/real-estate'

describe('Asset Real Estate API', () => {
  it('should request /asset/real-estate/list', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ rows: [], total: 0 })

    await getRealEstateList({ ownershipCertNo: 'CERT-001' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate/list',
      method: 'get',
      params: { ownershipCertNo: 'CERT-001' }
    })
  })

  it('should request /asset/real-estate/{assetId}', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: { assetId: 1001 } })

    await getRealEstateDetail(1001)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate/1001',
      method: 'get'
    })
  })
})


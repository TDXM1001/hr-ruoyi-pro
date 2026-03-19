import { vi, describe, it, expect } from 'vitest'

vi.mock('@/utils/http', () => {
  return {
    default: {
      request: vi.fn(),
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn()
    }
  }
})

import http from '@/utils/http'
import { listAssetDisposal, getAssetDisposal, addAssetDisposal } from '../../src/api/asset/disposal'

describe('Asset Disposal API', () => {
  it('should expose disposal methods', () => {
    expect(typeof listAssetDisposal).toBe('function')
    expect(typeof getAssetDisposal).toBe('function')
    expect(typeof addAssetDisposal).toBe('function')
  })

  it('should request disposal list with query params', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])

    await listAssetDisposal({ disposalStatus: 'CONFIRMED', pageNum: 1, pageSize: 10 })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/disposal/list',
      method: 'get',
      params: { disposalStatus: 'CONFIRMED', pageNum: 1, pageSize: 10 }
    })
  })

  it('should submit disposal payload', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce(101)
    const payload = {
      assetId: 10001,
      disposalType: 'SCRAP',
      disposalReason: '盘点毁损',
      disposalDate: '2026-03-19'
    }

    await addAssetDisposal(payload)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/disposal',
      method: 'post',
      data: payload
    })
  })
})

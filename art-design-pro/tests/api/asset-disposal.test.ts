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
import {
  listAssetDisposal,
  getAssetDisposal,
  addAssetDisposal,
  approveAssetDisposal,
  rejectAssetDisposal,
  listAssetDisposalApprovals
} from '../../src/api/asset/disposal'

describe('Asset Disposal API', () => {
  it('should expose disposal methods', () => {
    expect(typeof listAssetDisposal).toBe('function')
    expect(typeof getAssetDisposal).toBe('function')
    expect(typeof addAssetDisposal).toBe('function')
    expect(typeof approveAssetDisposal).toBe('function')
    expect(typeof rejectAssetDisposal).toBe('function')
    expect(typeof listAssetDisposalApprovals).toBe('function')
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

  it('should approve disposal with opinion payload', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ code: 200 })

    await approveAssetDisposal(11, { opinion: '同意处置' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/disposal/11/approve',
      method: 'post',
      data: { opinion: '同意处置' }
    })
  })

  it('should reject disposal with opinion payload', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ code: 200 })

    await rejectAssetDisposal(11, { opinion: '材料不完整' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/disposal/11/reject',
      method: 'post',
      data: { opinion: '材料不完整' }
    })
  })

  it('should request disposal approval records', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])

    await listAssetDisposalApprovals(11)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/disposal/11/approvals',
      method: 'get'
    })
  })
})

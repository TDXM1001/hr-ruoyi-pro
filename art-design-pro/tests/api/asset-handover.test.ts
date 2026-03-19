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
  listAssetHandover,
  addAssetHandover,
  listAssetHandoverOrder,
  getAssetHandoverOrder,
  listAssetHandoverItems,
  addAssetHandoverOrder
} from '../../src/api/asset/handover'

describe('Asset Handover API', () => {
  it('should expose handover methods', () => {
    expect(typeof listAssetHandover).toBe('function')
    expect(typeof addAssetHandover).toBe('function')
    expect(typeof listAssetHandoverOrder).toBe('function')
    expect(typeof getAssetHandoverOrder).toBe('function')
    expect(typeof listAssetHandoverItems).toBe('function')
    expect(typeof addAssetHandoverOrder).toBe('function')
  })

  it('should request order list with query params', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])

    await listAssetHandoverOrder({ handoverType: 'ASSIGN', pageNum: 1, pageSize: 10 })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/handover/order/list',
      method: 'get',
      params: { handoverType: 'ASSIGN', pageNum: 1, pageSize: 10 }
    })
  })

  it('should request order items by order id', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])

    await listAssetHandoverItems(1001)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/handover/order/1001/items',
      method: 'get'
    })
  })

  it('should request order detail by order id', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({})

    await getAssetHandoverOrder(1001)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/handover/order/1001',
      method: 'get'
    })
  })

  it('should create handover order with payload', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce(1002)
    const payload = {
      handoverType: 'TRANSFER' as const,
      handoverDate: '2026-03-19',
      assetIds: [10001, 10002],
      toDeptId: 103,
      toUserId: 1,
      locationName: '研发中心A区',
      remark: '批量调拨联调'
    }

    await addAssetHandoverOrder(payload)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/handover/order',
      method: 'post',
      data: payload
    })
  })
})

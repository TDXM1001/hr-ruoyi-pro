import { vi, describe, it, expect, beforeEach } from 'vitest'

vi.mock('@/utils/http', () => {
  return {
    default: {
      request: vi.fn()
    }
  }
})

import http from '@/utils/http'
import {
  addRealEstate,
  getRealEstateCategoryTree,
  getRealEstateDetail,
  getRealEstateLifecycle,
  getRealEstateList,
  getNextRealEstateCode,
  updateRealEstate
} from '../../src/api/asset/real-estate'

describe('Asset Real Estate API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

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

  it('should request /asset/real-estate/{assetId}/lifecycle', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: { ledger: {}, changeLogs: [] } })

    await getRealEstateLifecycle(1001)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate/1001/lifecycle',
      method: 'get'
    })
  })

  it('should request create real estate archive', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: 20001 })

    await addRealEstate({ assetName: '深圳研发办公楼A座' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate',
      method: 'post',
      data: { assetName: '深圳研发办公楼A座' }
    })
  })

  it('should request update real estate archive', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ code: 200 })

    await updateRealEstate({ assetId: 20001, assetName: '深圳研发办公楼A座-更新' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate',
      method: 'put',
      data: { assetId: 20001, assetName: '深圳研发办公楼A座-更新' }
    })
  })

  it('should request real estate category tree and next code', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([{ id: 1101, label: '办公用房' }])
    requestMock.mockResolvedValueOnce('RE-2026-0002')

    await getRealEstateCategoryTree()
    const code = await getNextRealEstateCode()

    expect(code).toBe('RE-2026-0002')
    expect(requestMock).toHaveBeenNthCalledWith(1, {
      url: '/asset/real-estate/categoryTree',
      method: 'get'
    })
    expect(requestMock).toHaveBeenNthCalledWith(2, {
      url: '/asset/real-estate/nextCode',
      method: 'get'
    })
  })
})

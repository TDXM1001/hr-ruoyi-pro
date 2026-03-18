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
  listAssetLedger,
  getAssetLedger,
  addAssetLedger,
  updateAssetLedger,
  exportAssetLedger,
  getAssetCategoryTree,
  getAssetDeptTree,
  getNextAssetCode,
  listAssetResponsibleUsers
} from '../../src/api/asset/ledger'

describe('Asset Ledger API', () => {
  it('should expose ledger page methods', () => {
    expect(typeof listAssetLedger).toBe('function')
    expect(typeof getAssetLedger).toBe('function')
    expect(typeof addAssetLedger).toBe('function')
    expect(typeof updateAssetLedger).toBe('function')
    expect(typeof exportAssetLedger).toBe('function')
  })

  it('should expose ledger option methods', () => {
    expect(typeof getAssetCategoryTree).toBe('function')
    expect(typeof getAssetDeptTree).toBe('function')
    expect(typeof getNextAssetCode).toBe('function')
    expect(typeof listAssetResponsibleUsers).toBe('function')
  })

  it('should expose ledger methods', () => {
    expect(typeof listAssetLedger).toBe('function')
    expect(typeof getAssetLedger).toBe('function')
    expect(typeof addAssetLedger).toBe('function')
    expect(typeof updateAssetLedger).toBe('function')
    expect(typeof exportAssetLedger).toBe('function')
  })

  it('should request responsible users with query params', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])

    await listAssetResponsibleUsers({ keyword: '张三' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/ledger/responsibleUsers',
      method: 'get',
      params: { keyword: '张三' }
    })
  })

  it('should request next asset code from backend', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce('FA-2026-0004')

    const result = await getNextAssetCode()

    expect(result).toBe('FA-2026-0004')
    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/ledger/nextCode',
      method: 'get'
    })
  })

  it('should normalize next asset code when backend response is wrapped object', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: 'FA-2026-0005' })

    const result = await getNextAssetCode()

    expect(result).toBe('FA-2026-0005')
  })

  it('should normalize next asset code when backend returns msg field', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ code: 200, msg: 'FA-2026-0006' })

    const result = await getNextAssetCode()

    expect(result).toBe('FA-2026-0006')
  })
})

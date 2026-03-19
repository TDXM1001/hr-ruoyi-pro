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
  listInventoryTask,
  getInventoryTask,
  addInventoryTask,
  submitInventoryResult
} from '../../src/api/asset/inventory'

describe('Asset Inventory API', () => {
  it('should expose inventory methods', () => {
    expect(typeof listInventoryTask).toBe('function')
    expect(typeof getInventoryTask).toBe('function')
    expect(typeof addInventoryTask).toBe('function')
    expect(typeof submitInventoryResult).toBe('function')
  })

  it('should request inventory task list with query params', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])

    await listInventoryTask({ taskStatus: 'IN_PROGRESS', pageNum: 1, pageSize: 10 })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/inventory/task/list',
      method: 'get',
      params: { taskStatus: 'IN_PROGRESS', pageNum: 1, pageSize: 10 }
    })
  })

  it('should submit inventory result with payload', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce(undefined)
    const payload = {
      taskId: 10,
      assetId: 10001,
      inventoryResult: 'NORMAL',
      followUpAction: 'NONE'
    }

    await submitInventoryResult(payload)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/inventory/result',
      method: 'post',
      data: payload
    })
  })
})

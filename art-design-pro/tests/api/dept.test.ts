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

import { listDept } from '../../src/api/system/dept'

describe('Dept API', () => {
  it('should expose listDept method', () => {
    expect(typeof listDept).toBe('function')
  })
})

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

import { listUser } from '../../src/api/system/user'

describe('User API', () => {
  it('should expose listUser method', () => {
    expect(typeof listUser).toBe('function')
  })
})

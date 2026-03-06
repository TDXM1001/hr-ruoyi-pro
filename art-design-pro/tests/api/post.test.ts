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

import { listPost } from '../../src/api/system/post'

describe('Post API', () => {
  it('should expose listPost method', () => {
    expect(typeof listPost).toBe('function')
  })
})

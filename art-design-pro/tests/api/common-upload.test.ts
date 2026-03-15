import { beforeEach, describe, expect, it, vi } from 'vitest'

const { http } = vi.hoisted(() => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
    request: vi.fn()
  }
}))

vi.mock('@/utils/http', () => ({
  default: http
}))

import { uploadCommonFile } from '../../src/api/common/upload'

describe('Common Upload API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('posts file data to common upload endpoint', async () => {
    const file = new File(['demo'], 'demo.txt', { type: 'text/plain' })

    await uploadCommonFile(file)

    expect(http.request).toHaveBeenCalledTimes(1)
    expect(http.request).toHaveBeenCalledWith(
      expect.objectContaining({
        url: '/common/upload',
        method: 'POST',
        data: expect.any(FormData)
      })
    )
  })
})

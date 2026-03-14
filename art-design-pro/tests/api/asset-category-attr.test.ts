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

import { listCategoryAttrs } from '../../src/api/asset/category-attr'

describe('Asset Category Attr API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads dynamic attributes by category', async () => {
    await listCategoryAttrs(10)

    expect(http.get).toHaveBeenCalledWith({ url: '/asset/categoryAttr/category/10' })
  })
})

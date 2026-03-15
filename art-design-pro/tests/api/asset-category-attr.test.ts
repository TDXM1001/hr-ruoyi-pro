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

import {
  addCategoryAttr,
  delCategoryAttr,
  disableCategoryAttr,
  getCategoryAttr,
  listCategoryAttrs,
  updateCategoryAttr
} from '../../src/api/asset/category-attr'

describe('Asset Category Attr API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads dynamic attributes by category', async () => {
    await listCategoryAttrs(10)

    expect(http.get).toHaveBeenCalledWith({ url: '/asset/categoryAttr/category/10' })
  })

  it('supports attr crud routes', async () => {
    await getCategoryAttr(1)
    await addCategoryAttr({ categoryId: 10, attrCode: 'manufacturer', attrName: '厂商' })
    await updateCategoryAttr({
      attrId: 1,
      categoryId: 10,
      attrCode: 'manufacturer',
      attrName: '生产厂商'
    })
    await disableCategoryAttr(1)
    await delCategoryAttr([1, 2])

    expect(http.get).toHaveBeenCalledWith({ url: '/asset/categoryAttr/1' })
    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/categoryAttr',
      data: { categoryId: 10, attrCode: 'manufacturer', attrName: '厂商' }
    })
    expect(http.put).toHaveBeenCalledWith({
      url: '/asset/categoryAttr',
      data: {
        attrId: 1,
        categoryId: 10,
        attrCode: 'manufacturer',
        attrName: '生产厂商'
      }
    })
    expect(http.put).toHaveBeenCalledWith({ url: '/asset/categoryAttr/disable/1' })
    expect(http.del).toHaveBeenCalledWith({ url: '/asset/categoryAttr/1,2' })
  })
})

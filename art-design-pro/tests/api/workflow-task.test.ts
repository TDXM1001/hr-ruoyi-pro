import { beforeEach, describe, expect, expectTypeOf, it, vi } from 'vitest'

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
  approveTask,
  listDone,
  listTodo,
  mergeWorkflowBusinessTypeOptions
} from '../../src/api/workflow/task'

describe('Workflow Task API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts businessId in task query type', () => {
    expectTypeOf<Parameters<typeof listTodo>[0]>().toEqualTypeOf<
      | {
          pageNum?: number
          pageSize?: number
          businessId?: string
          businessType?: string
          status?: string
        }
      | undefined
    >()
  })

  it('requests todo list with query params', async () => {
    await listTodo({ pageNum: 1, pageSize: 10, businessId: 'REQ-20260316-001' })

    expect(http.get).toHaveBeenCalledWith({
      url: '/workflow/task/todo',
      params: { pageNum: 1, pageSize: 10, businessId: 'REQ-20260316-001' }
    })
  })

  it('requests done list with query params', async () => {
    await listDone({ businessType: 'REAL_ESTATE_DISPOSAL' })

    expect(http.get).toHaveBeenCalledWith({
      url: '/workflow/task/done',
      params: { businessType: 'REAL_ESTATE_DISPOSAL' }
    })
  })

  it('posts approval request payload', async () => {
    await approveTask({ instanceId: 1001, action: 'approve', comment: '同意处理' })

    expect(http.post).toHaveBeenCalledWith({
      url: '/workflow/task/approve',
      data: { instanceId: 1001, action: 'approve', comment: '同意处理' }
    })
  })

  it('merges backend dictionary with local workflow fallbacks', () => {
    const options = mergeWorkflowBusinessTypeOptions([{ label: '资产领用-后端字典', value: 'REQUISITION' }])

    expect(options).toEqual(
      expect.arrayContaining([
        { label: '资产领用-后端字典', value: 'REQUISITION' },
        { label: '不动产权属变更', value: 'REAL_ESTATE_OWNERSHIP_CHANGE' }
      ])
    )
  })
})

import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
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
  const workflowTaskSource = readFileSync(resolve(process.cwd(), 'src/api/workflow/task.ts'), 'utf8')

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('accepts unified workflow query aliases', () => {
    expectTypeOf<Parameters<typeof listTodo>[0]>().toEqualTypeOf<
      | {
          pageNum?: number
          pageSize?: number
          businessId?: string
          businessType?: string
          status?: string
          bizNo?: string
          bizType?: string
          wfStatus?: string
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
    await approveTask({ instanceId: 1001, action: 'approve', comment: '审批通过' })

    expect(http.post).toHaveBeenCalledWith({
      url: '/workflow/task/approve',
      data: { instanceId: 1001, action: 'approve', comment: '审批通过' }
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

  it('locks workflow task alias contract for bizNo bizType and wfStatus', () => {
    expect(workflowTaskSource).toContain('bizNo?: string')
    expect(workflowTaskSource).toContain('bizType?: string')
    expect(workflowTaskSource).toContain('wfStatus?: string')
  })
})

import request from '@/utils/http'

/**
 * 待办任务查询参数
 */
export interface TaskQuery {
  pageNum?: number
  pageSize?: number
  businessType?: string
  status?: string
}

/**
 * 审批请求参数
 */
export interface ApproveReq {
  instanceId: number
  action: 'approve' | 'reject' | 'transfer'
  comment?: string
  assigneeId?: number
}

/**
 * 查询待办任务列表
 * @param params 查询参数
 */
export function listTodo(params?: TaskQuery) {
  return request.get<any[]>({
    url: '/workflow/task/todo',
    params
  })
}

/**
 * 查询已办任务列表
 * @param params 查询参数
 */
export function listDone(params?: TaskQuery) {
  return request.get<any[]>({
    url: '/workflow/task/done',
    params
  })
}

/**
 * 审批任务
 * @param data 审批参数
 */
export function approveTask(data: ApproveReq) {
  return request.post({
    url: '/workflow/task/approve',
    data
  })
}

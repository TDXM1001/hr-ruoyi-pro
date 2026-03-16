import request from '@/utils/http'

export interface WorkflowTaskOption {
  label: string
  value: string
}

/**
 * 工作流任务项。
 */
export interface WorkflowTaskItem {
  instanceId: number
  businessId: string
  businessType: string
  currentNode: string
  status: string
  action?: string
  comment?: string
  approverId?: number
  approverName?: string
  createTime?: string
}

/**
 * 待办任务查询参数
 */
export interface TaskQuery {
  pageNum?: number
  pageSize?: number
  businessId?: string
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
 * 工作流业务类型兜底字典。
 *
 * 后端在审批中心会把业务类型统一映射成大写编码，
 * 这里补齐不动产动作的前端显示兜底，避免 DictTag 只渲染原始编码。
 */
export const WORKFLOW_FALLBACK_BUSINESS_TYPE_OPTIONS: WorkflowTaskOption[] = [
  { label: '资产领用', value: 'REQUISITION' },
  { label: '资产归还', value: 'RETURN' },
  { label: '资产维修', value: 'REPAIR' },
  { label: '资产报废/处置', value: 'SCRAP' },
  { label: '不动产权属变更', value: 'REAL_ESTATE_OWNERSHIP_CHANGE' },
  { label: '不动产用途变更', value: 'REAL_ESTATE_USAGE_CHANGE' },
  { label: '不动产状态变更', value: 'REAL_ESTATE_STATUS_CHANGE' },
  { label: '不动产处置', value: 'REAL_ESTATE_DISPOSAL' }
]

/**
 * 合并后端字典与本地兜底项，优先使用后端字典文案。
 */
export function mergeWorkflowBusinessTypeOptions(
  options: WorkflowTaskOption[] = []
): WorkflowTaskOption[] {
  const optionMap = new Map<string, WorkflowTaskOption>()

  WORKFLOW_FALLBACK_BUSINESS_TYPE_OPTIONS.forEach((item) => {
    optionMap.set(item.value, item)
  })
  options.forEach((item) => {
    if (!item?.value) return
    optionMap.set(item.value, item)
  })

  return Array.from(optionMap.values())
}

/**
 * 查询待办任务列表
 * @param params 查询参数
 */
export function listTodo(params?: TaskQuery) {
  return request.get<WorkflowTaskItem[]>({
    url: '/workflow/task/todo',
    params
  })
}

/**
 * 查询已办任务列表
 * @param params 查询参数
 */
export function listDone(params?: TaskQuery) {
  return request.get<WorkflowTaskItem[]>({
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

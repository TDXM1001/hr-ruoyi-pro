import request from '@/utils/http'

export interface WorkflowTaskOption {
  label: string
  value: string
}

/**
 * 流程任务项。
 *
 * 同时保留历史字段和统一别名字段，方便流程中心平滑切换。
 */
export interface WorkflowTaskItem {
  instanceId: number
  businessId: string
  businessType: string
  status: string
  /** 统一业务单号别名。 */
  bizNo?: string
  /** 统一业务类型别名。 */
  bizType?: string
  /** 统一流程状态别名。 */
  wfStatus?: string
  currentNode: string
  action?: string
  comment?: string
  approverId?: number
  approverName?: string
  createTime?: string
}

/**
 * 流程任务查询参数。
 */
export interface TaskQuery {
  pageNum?: number
  pageSize?: number
  businessId?: string
  businessType?: string
  status?: string
  /** 统一业务单号。 */
  bizNo?: string
  /** 统一业务类型。 */
  bizType?: string
  /** 统一流程状态。 */
  wfStatus?: string
}

/**
 * 审批请求。
 */
export interface ApproveReq {
  instanceId: number
  action: 'approve' | 'reject' | 'transfer'
  comment?: string
  assigneeId?: number
}

/**
 * 当字典尚未加载完成时，前端本地兜底的业务类型选项。
 */
export const WORKFLOW_FALLBACK_BUSINESS_TYPE_OPTIONS: WorkflowTaskOption[] = [
  { label: '资产领用', value: 'REQUISITION' },
  { label: '资产归还', value: 'RETURN' },
  { label: '资产维修', value: 'REPAIR' },
  { label: '资产处置', value: 'SCRAP' },
  { label: '不动产权属变更', value: 'REAL_ESTATE_OWNERSHIP_CHANGE' },
  { label: '不动产用途变更', value: 'REAL_ESTATE_USAGE_CHANGE' },
  { label: '不动产状态变更', value: 'REAL_ESTATE_STATUS_CHANGE' },
  { label: '不动产处置', value: 'REAL_ESTATE_DISPOSAL' }
]

/**
 * 合并后端字典与前端兜底选项。
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
 * 查询待办任务。
 */
export function listTodo(params?: TaskQuery) {
  return request.get<WorkflowTaskItem[]>({
    url: '/workflow/task/todo',
    params
  })
}

/**
 * 查询已办任务。
 */
export function listDone(params?: TaskQuery) {
  return request.get<WorkflowTaskItem[]>({
    url: '/workflow/task/done',
    params
  })
}

/**
 * 提交审批。
 */
export function approveTask(data: ApproveReq) {
  return request.post({
    url: '/workflow/task/approve',
    data
  })
}
